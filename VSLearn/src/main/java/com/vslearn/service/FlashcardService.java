package com.vslearn.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.vslearn.dto.response.FlashcardDTO;
import com.vslearn.entities.VocabArea;
import com.vslearn.repository.VocabAreaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FlashcardService {

    private final Storage storage;
    private final String bucketName; 
    private final VocabAreaRepository vocabAreaRepository;

    public FlashcardService(
            VocabAreaRepository vocabAreaRepository,
            @Value("${gcp.storage.credentials.location}") String credentialsPath,
            @Value("${gcp.storage.bucket.name}") String bucketName
    ) throws IOException {
        this.vocabAreaRepository = vocabAreaRepository;
        this.bucketName = bucketName;
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath.replace("file:", "")));
        this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    public List<FlashcardDTO> getFlashcardsForSubtopic(String subtopicId) {
        System.out.println("=== FlashcardService.getFlashcardsForSubtopic() called with subtopicId: " + subtopicId + " ===");
        
        List<VocabArea> vocabAreas = vocabAreaRepository.findByVocabSubTopicId(Long.parseLong(subtopicId));
        System.out.println("Found " + vocabAreas.size() + " vocab areas for subtopic " + subtopicId);
        
        return vocabAreas.stream().map(va -> {
            String word = va.getVocab().getVocab();
            String description = va.getVocabAreaDescription() != null ? va.getVocabAreaDescription() : "";
            String objectName = va.getVocabAreaGif();
            URL signedUrl = generateSignedUrl(objectName);
            
            System.out.println("Processing vocab: " + word + " with gif: " + objectName);

            FlashcardDTO.FrontDTO front = new FlashcardDTO.FrontDTO("image", signedUrl != null ? signedUrl.toString() : "", word);
            FlashcardDTO.BackDTO back = new FlashcardDTO.BackDTO(word.toUpperCase(), description);
            return new FlashcardDTO(va.getId(), front, back);
        }).collect(Collectors.toList());
    }

    public List<FlashcardDTO> getFlashcardsForArea(String areaId) {
        List<VocabArea> vocabAreas = vocabAreaRepository.findByAreaId(Long.parseLong(areaId));
        return vocabAreas.stream().map(va -> {
            String word = va.getVocab().getVocab();
            String description = va.getVocabAreaDescription() != null ? va.getVocabAreaDescription() : "";
            String objectName = va.getVocabAreaGif();
            URL signedUrl = generateSignedUrl(objectName);

            FlashcardDTO.FrontDTO front = new FlashcardDTO.FrontDTO("image", signedUrl != null ? signedUrl.toString() : "", word);
            FlashcardDTO.BackDTO back = new FlashcardDTO.BackDTO(word.toUpperCase(), description);
            return new FlashcardDTO(va.getId(), front, back);
        }).collect(Collectors.toList());
    }

    public long getWordCountBySubtopicId(Long subtopicId) {
        List<VocabArea> vocabAreas = vocabAreaRepository.findByVocabSubTopicId(subtopicId);
        return vocabAreas.size();
    }

    private URL generateSignedUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        try {
            return storage.signUrl(blobInfo, 15, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}