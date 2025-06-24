package com.vslearn.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.vslearn.dto.response.FlashcardDTO;
import com.vslearn.dto.response.PracticeQuestionDTO;
import com.vslearn.dto.response.TimelineResponseDTO;
import com.vslearn.dto.response.PracticeQuestionsResponseDTO;
import com.vslearn.entities.VocabArea;
import com.vslearn.repository.VocabAreaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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

    // Generate timeline cho subtopic
    public TimelineResponseDTO generateTimeline(String subtopicId) {
        List<VocabArea> vocabAreas = vocabAreaRepository.findByVocabSubTopicId(Long.parseLong(subtopicId));
        int totalCards = vocabAreas.size();

        System.out.println("🔧 Backend: Creating timeline for " + totalCards + " cards");
        
        // Logic đơn giản: chia đều thành 3-4 nhóm
        int numGroups = 3; // Bắt đầu với 3 nhóm
        
        if (totalCards <= 9) {
            numGroups = 3;
        } else if (totalCards <= 12) {
            numGroups = 4;
        } else {
            numGroups = 4; // Tối đa 4 nhóm
        }
        
        int groupSize = (int) Math.ceil((double) totalCards / numGroups);
        
        System.out.println("  - numGroups: " + numGroups);
        System.out.println("  - groupSize: " + groupSize);

        List<TimelineResponseDTO.TimelineStepDTO> timeline = new ArrayList<>();
        int i = 0;
        
        // Tạo các nhóm flashcard và practice
        for (int group = 0; group < numGroups; group++) {
            int groupStart = i;
            int remainingCards = totalCards - i;
            
            if (remainingCards <= 0) break;
            
            int currentGroupSize = Math.min(groupSize, remainingCards);
            
            System.out.println("  - Group " + (group + 1) + ": taking " + currentGroupSize + " cards (" + i + " to " + (i + currentGroupSize - 1) + ")");
            
            // Thêm flashcards cho nhóm này
            for (int j = 0; j < currentGroupSize; j++) {
                timeline.add(new TimelineResponseDTO.TimelineStepDTO("flashcard", i, null, null));
                i++;
            }
            
            // Thêm practice cho nhóm vừa thêm
            System.out.println("    - Adding practice for cards " + groupStart + " to " + i);
            timeline.add(new TimelineResponseDTO.TimelineStepDTO("practice", null, groupStart, i));
        }
        
        System.out.println("📊 Backend: Final timeline size: " + timeline.size());
        System.out.println("✅ Total cards covered: " + i + "/" + totalCards);
        
        // Kiểm tra cuối cùng
        if (i != totalCards) {
            System.err.println("❌ ERROR: Expected " + totalCards + " cards but got " + i);
        }
        
        return new TimelineResponseDTO(timeline, 0, "default-user", subtopicId);
    }

    // Generate practice questions cho range cụ thể
    public PracticeQuestionsResponseDTO generatePracticeQuestions(String subtopicId, int start, int end) {
        List<VocabArea> vocabAreas = vocabAreaRepository.findByVocabSubTopicId(Long.parseLong(subtopicId));
        List<VocabArea> practiceRange = vocabAreas.subList(start, Math.min(end, vocabAreas.size()));
        List<VocabArea> allVocabAreas = new ArrayList<>(vocabAreas);
        
        List<PracticeQuestionsResponseDTO.PracticeQuestionDTO> questions = new ArrayList<>();
        
        for (VocabArea va : practiceRange) {
            String correctWord = va.getVocab().getVocab();
            String objectName = va.getVocabAreaGif();
            URL signedUrl = generateSignedUrl(objectName);
            String videoUrl = signedUrl != null ? signedUrl.toString() : "";
            String imageUrl = videoUrl;
            
            // Sinh distractors (đáp án sai)
            List<VocabArea> distractorAreas = new ArrayList<>(allVocabAreas);
            distractorAreas.remove(va);
            Collections.shuffle(distractorAreas);
            
            List<PracticeQuestionsResponseDTO.PracticeQuestionDTO.OptionDTO> options = new ArrayList<>();
            
            // Đáp án đúng
            options.add(new PracticeQuestionsResponseDTO.PracticeQuestionDTO.OptionDTO(correctWord, videoUrl, imageUrl));
            
            // Đáp án sai (tối đa 3)
            for (int i = 0; i < Math.min(3, distractorAreas.size()); i++) {
                VocabArea wrongVa = distractorAreas.get(i);
                String wrongWord = wrongVa.getVocab().getVocab();
                String wrongObjectName = wrongVa.getVocabAreaGif();
                URL wrongSignedUrl = generateSignedUrl(wrongObjectName);
                String wrongVideoUrl = wrongSignedUrl != null ? wrongSignedUrl.toString() : "";
                String wrongImageUrl = wrongVideoUrl;
                options.add(new PracticeQuestionsResponseDTO.PracticeQuestionDTO.OptionDTO(wrongWord, wrongVideoUrl, wrongImageUrl));
            }
            
            Collections.shuffle(options);
            
            PracticeQuestionsResponseDTO.PracticeQuestionDTO question = new PracticeQuestionsResponseDTO.PracticeQuestionDTO(
                va.getId(),
                videoUrl,
                imageUrl,
                "Đây là từ gì?",
                options,
                correctWord
            );
            questions.add(question);
        }
        
        // Random thứ tự câu hỏi
        Collections.shuffle(questions);
        
        return new PracticeQuestionsResponseDTO(questions, start, end, subtopicId);
    }

    public List<PracticeQuestionDTO> getPracticeQuestionsForSubtopic(String subtopicId) {
        List<VocabArea> vocabAreas = vocabAreaRepository.findByVocabSubTopicId(Long.parseLong(subtopicId));
        List<PracticeQuestionDTO> questions = new ArrayList<>();
        List<VocabArea> allVocabAreas = new ArrayList<>(vocabAreas);
        for (VocabArea va : vocabAreas) {
            String correctWord = va.getVocab().getVocab();
            String objectName = va.getVocabAreaGif();
            URL signedUrl = generateSignedUrl(objectName);
            String videoUrl = signedUrl != null ? signedUrl.toString() : "";
            String imageUrl = videoUrl;
            // Sinh 3 đáp án sai random
            List<VocabArea> distractorAreas = new ArrayList<>(allVocabAreas);
            distractorAreas.remove(va);
            Collections.shuffle(distractorAreas);
            List<PracticeQuestionDTO.OptionDTO> options = new ArrayList<>();
            // Đáp án đúng
            options.add(new PracticeQuestionDTO.OptionDTO(correctWord, videoUrl, imageUrl));
            // Đáp án sai
            for (int i = 0; i < Math.min(3, distractorAreas.size()); i++) {
                VocabArea wrongVa = distractorAreas.get(i);
                String wrongWord = wrongVa.getVocab().getVocab();
                String wrongObjectName = wrongVa.getVocabAreaGif();
                URL wrongSignedUrl = generateSignedUrl(wrongObjectName);
                String wrongVideoUrl = wrongSignedUrl != null ? wrongSignedUrl.toString() : "";
                String wrongImageUrl = wrongVideoUrl;
                options.add(new PracticeQuestionDTO.OptionDTO(wrongWord, wrongVideoUrl, wrongImageUrl));
            }
            Collections.shuffle(options);
            PracticeQuestionDTO q = new PracticeQuestionDTO(
                va.getId(),
                videoUrl,
                imageUrl,
                "Đây là từ gì?",
                options,
                correctWord
            );
            questions.add(q);
        }
        return questions;
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