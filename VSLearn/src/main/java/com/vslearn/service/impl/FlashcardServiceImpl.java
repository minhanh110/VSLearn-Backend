package com.vslearn.service.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.vslearn.dto.response.FlashcardDTO;
import com.vslearn.dto.response.PracticeQuestionDTO;
import com.vslearn.dto.response.TimelineResponseDTO;
import com.vslearn.dto.response.PracticeQuestionsResponseDTO;
import com.vslearn.dto.response.SubtopicInfoDTO;
import com.vslearn.dto.response.FlashcardProgressResponse;
import com.vslearn.dto.request.FlashcardProgressSaveRequest;
import com.vslearn.dto.response.SentenceBuildingQuestionDTO;
import com.vslearn.entities.VocabArea;
import com.vslearn.entities.Progress;
import com.vslearn.entities.User;
import com.vslearn.entities.SubTopic;
import com.vslearn.entities.Sentence;
import com.vslearn.entities.SentenceVocab;
import com.vslearn.repository.VocabAreaRepository;
import com.vslearn.repository.SubTopicRepository;
import com.vslearn.repository.ProgressRepository;
import com.vslearn.repository.UserRepository;
import com.vslearn.repository.SentenceRepository;
import com.vslearn.repository.SentenceVocabRepository;
import com.vslearn.repository.WordRepository;
import com.vslearn.service.FlashcardService;
import com.vslearn.exception.customizeException.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Optional;
import java.time.Instant;

@Service
public class FlashcardServiceImpl implements FlashcardService {
    private final Storage storage;
    private final String bucketName;
    private final VocabAreaRepository vocabAreaRepository;
    private final SubTopicRepository subTopicRepository;
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final SentenceRepository sentenceRepository;
    private final SentenceVocabRepository sentenceVocabRepository;
    private final WordRepository wordRepository;

    public FlashcardServiceImpl(
            VocabAreaRepository vocabAreaRepository,
            SubTopicRepository subTopicRepository,
            ProgressRepository progressRepository,
            UserRepository userRepository,
            SentenceRepository sentenceRepository,
            SentenceVocabRepository sentenceVocabRepository,
            WordRepository wordRepository,
            @Value("${gcp.storage.credentials.location}") String credentialsPath,
            @Value("${gcp.storage.bucket.name}") String bucketName,
            ObjectMapper objectMapper
    ) throws IOException {
        this.vocabAreaRepository = vocabAreaRepository;
        this.subTopicRepository = subTopicRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.sentenceRepository = sentenceRepository;
        this.sentenceVocabRepository = sentenceVocabRepository;
        this.wordRepository = wordRepository;
        this.bucketName = bucketName;
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath.replace("file:", "")));
        this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        this.objectMapper = objectMapper;
    }

    @Override
    public List<FlashcardDTO> getFlashcardsForSubtopic(String subtopicId) {
        System.out.println("=== FlashcardService.getFlashcardsForSubtopic() called with subtopicId: " + subtopicId + " ===");
        List<VocabArea> vocabAreas = vocabAreaRepository.findByVocabSubTopicId(Long.parseLong(subtopicId));
        System.out.println("Found " + vocabAreas.size() + " vocab areas for subtopic " + subtopicId);
        return vocabAreas.stream().map(va -> {
            String word = va.getVocab().getVocab();
            String description = va.getVocabAreaDescription() != null ? va.getVocabAreaDescription() : "";
            String objectName = va.getVocabAreaVideo();
            URL signedUrl = generateSignedUrl(objectName);
            System.out.println("Processing vocab: " + word + " with video: " + objectName);
            FlashcardDTO.FrontDTO front = new FlashcardDTO.FrontDTO("video", signedUrl != null ? signedUrl.toString() : "", word);
            FlashcardDTO.BackDTO back = new FlashcardDTO.BackDTO(word.toUpperCase(), description);
            return new FlashcardDTO(va.getId(), front, back);
        }).collect(Collectors.toList());
    }

    @Override
    public List<FlashcardDTO> getFlashcardsForArea(String areaId) {
        List<VocabArea> vocabAreas = vocabAreaRepository.findByAreaId(Long.parseLong(areaId));
        return vocabAreas.stream().map(va -> {
            String word = va.getVocab().getVocab();
            String description = va.getVocabAreaDescription() != null ? va.getVocabAreaDescription() : "";
            String objectName = va.getVocabAreaVideo();
            URL signedUrl = generateSignedUrl(objectName);
            FlashcardDTO.FrontDTO front = new FlashcardDTO.FrontDTO("video", signedUrl != null ? signedUrl.toString() : "", word);
            FlashcardDTO.BackDTO back = new FlashcardDTO.BackDTO(word.toUpperCase(), description);
            return new FlashcardDTO(va.getId(), front, back);
        }).collect(Collectors.toList());
    }

    @Override
    public long getWordCountBySubtopicId(Long subtopicId) {
        List<VocabArea> vocabAreas = vocabAreaRepository.findByVocabSubTopicId(subtopicId);
        return vocabAreas.size();
    }

    @Override
    public TimelineResponseDTO generateTimeline(String subtopicId) {
        List<VocabArea> vocabAreas = vocabAreaRepository.findByVocabSubTopicId(Long.parseLong(subtopicId));
        int totalCards = vocabAreas.size();
        System.out.println("🔧 Backend: Creating timeline for " + totalCards + " cards");
        int numGroups = 3;
        if (totalCards <= 9) {
            numGroups = 3;
        } else if (totalCards <= 12) {
            numGroups = 4;
        } else {
            numGroups = 4;
        }
        int groupSize = (int) Math.ceil((double) totalCards / numGroups);
        System.out.println("  - numGroups: " + numGroups);
        System.out.println("  - groupSize: " + groupSize);
        List<TimelineResponseDTO.TimelineStepDTO> timeline = new ArrayList<>();
        int i = 0;
        for (int group = 0; group < numGroups; group++) {
            int groupStart = i;
            int remainingCards = totalCards - i;
            if (remainingCards <= 0) break;
            int currentGroupSize = Math.min(groupSize, remainingCards);
            System.out.println("  - Group " + (group + 1) + ": taking " + currentGroupSize + " cards (" + i + " to " + (i + currentGroupSize - 1) + ")");
            for (int j = 0; j < currentGroupSize; j++) {
                timeline.add(new TimelineResponseDTO.TimelineStepDTO("flashcard", i, null, null));
                i++;
            }
            System.out.println("    - Adding practice for cards " + groupStart + " to " + i);
            timeline.add(new TimelineResponseDTO.TimelineStepDTO("practice", null, groupStart, i));
        }
        System.out.println("📊 Backend: Final timeline size: " + timeline.size());
        System.out.println("✅ Total cards covered: " + i + "/" + totalCards);
        if (i != totalCards) {
            System.err.println("❌ ERROR: Expected " + totalCards + " cards but got " + i);
        }
        return new TimelineResponseDTO(timeline, 0, "default-user", subtopicId);
    }

    @Override
    public PracticeQuestionsResponseDTO generatePracticeQuestions(String subtopicId, int start, int end) {
        List<VocabArea> vocabAreas = vocabAreaRepository.findByVocabSubTopicId(Long.parseLong(subtopicId));
        List<VocabArea> practiceRange = vocabAreas.subList(start, Math.min(end, vocabAreas.size()));
        List<VocabArea> allVocabAreas = new ArrayList<>(vocabAreas);
        List<PracticeQuestionsResponseDTO.PracticeQuestionDTO> questions = new ArrayList<>();
        for (VocabArea va : practiceRange) {
            String correctWord = va.getVocab().getVocab();
            String objectName = va.getVocabAreaVideo();
            URL signedUrl = generateSignedUrl(objectName);
            String videoUrl = signedUrl != null ? signedUrl.toString() : "";
            String imageUrl = videoUrl;
            List<VocabArea> distractorAreas = new ArrayList<>(allVocabAreas);
            distractorAreas.remove(va);
            Collections.shuffle(distractorAreas);
            List<PracticeQuestionsResponseDTO.PracticeQuestionDTO.OptionDTO> options = new ArrayList<>();
            options.add(new PracticeQuestionsResponseDTO.PracticeQuestionDTO.OptionDTO(correctWord, videoUrl, imageUrl));
            for (int i = 0; i < Math.min(3, distractorAreas.size()); i++) {
                VocabArea wrongVa = distractorAreas.get(i);
                String wrongWord = wrongVa.getVocab().getVocab();
                String wrongObjectName = wrongVa.getVocabAreaVideo();
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
        Collections.shuffle(questions);
        return new PracticeQuestionsResponseDTO(questions, start, end, subtopicId);
    }

    @Override
    public List<PracticeQuestionDTO> getPracticeQuestionsForSubtopic(String subtopicId) {
        List<VocabArea> vocabAreas = vocabAreaRepository.findByVocabSubTopicId(Long.parseLong(subtopicId));
        List<PracticeQuestionDTO> questions = new ArrayList<>();
        List<VocabArea> allVocabAreas = new ArrayList<>(vocabAreas);
        for (VocabArea va : vocabAreas) {
            String correctWord = va.getVocab().getVocab();
            String objectName = va.getVocabAreaVideo();
            URL signedUrl = generateSignedUrl(objectName);
            String videoUrl = signedUrl != null ? signedUrl.toString() : "";
            String imageUrl = videoUrl;
            List<VocabArea> distractorAreas = new ArrayList<>(allVocabAreas);
            distractorAreas.remove(va);
            Collections.shuffle(distractorAreas);
            List<PracticeQuestionDTO.OptionDTO> options = new ArrayList<>();
            options.add(new PracticeQuestionDTO.OptionDTO(correctWord, videoUrl, imageUrl));
            for (int i = 0; i < Math.min(3, distractorAreas.size()); i++) {
                VocabArea wrongVa = distractorAreas.get(i);
                String wrongWord = wrongVa.getVocab().getVocab();
                String wrongObjectName = wrongVa.getVocabAreaVideo();
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

    @Override
    public SubtopicInfoDTO getSubtopicInfo(String subtopicId) {
        Optional<com.vslearn.entities.SubTopic> subTopic = subTopicRepository.findById(Long.parseLong(subtopicId));
        if (subTopic.isEmpty()) {
            throw new ResourceNotFoundException("Subtopic not found", subtopicId);
        }
        com.vslearn.entities.SubTopic st = subTopic.get();
        return new SubtopicInfoDTO(
            st.getId(),
            st.getSubTopicName(),
            st.getTopic().getTopicName(),
            st.getStatus()
        );
    }

    private URL generateSignedUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        try {
            return storage.signUrl(blobInfo, 2, TimeUnit.HOURS, Storage.SignUrlOption.withV4Signature());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public FlashcardProgressResponse saveProgress(String subtopicId, FlashcardProgressSaveRequest request) {
        try {
            Long subTopicId = Long.parseLong(subtopicId);
            Long userId = Long.parseLong(request.getUserId());
            
            // Lấy subtopic và user
            SubTopic subTopic = subTopicRepository.findById(subTopicId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtopic not found", subTopicId));
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", userId));
            
            // Kiểm tra xem đã có progress chưa
            Optional<Progress> existingProgress = progressRepository.findByCreatedBy_IdAndSubTopic_Id(userId, subTopicId);
            
            if (existingProgress.isPresent()) {
                // Cập nhật progress hiện có
                Progress progress = existingProgress.get();
                progress.setIsComplete(true);
                progressRepository.save(progress);
            } else {
                // Tạo progress mới
                Progress progress = Progress.builder()
                    .subTopic(subTopic)
                    .createdBy(user)
                    .isComplete(true)
                    .createdAt(Instant.now())
                    .build();
                progressRepository.save(progress);
            }
            
            return new FlashcardProgressResponse(
                true,
                "Progress saved successfully",
                request.getCompletedFlashcards(),
                request.getCompletedPractice(),
                request.getUserChoice(),
                100 // Đã hoàn thành subtopic
            );
            
        } catch (Exception e) {
            e.printStackTrace();
            return new FlashcardProgressResponse(
                false,
                "Error saving progress: " + e.getMessage(),
                request.getCompletedFlashcards(),
                request.getCompletedPractice(),
                request.getUserChoice(),
                0
            );
        }
    }

    @Override
    public FlashcardProgressResponse getProgress(String subtopicId) {
        return null;
    }

    @Override
    public FlashcardProgressResponse getProgress(String subtopicId, String userId) {
        // Implementation for getProgress without userId parameter
        return new FlashcardProgressResponse();
    }

    @Override
    public List<SentenceBuildingQuestionDTO> getSentenceBuildingQuestions(String subtopicId) {
        // Lấy subtopic để biết topic ID
        Optional<SubTopic> subTopic = subTopicRepository.findById(Long.parseLong(subtopicId));
        if (subTopic.isEmpty()) {
            return new ArrayList<>();
        }
        
        Long topicId = subTopic.get().getTopic().getId();
        List<Sentence> sentences = sentenceRepository.findBySentenceTopicId(topicId);
        
        List<SentenceBuildingQuestionDTO> questions = new ArrayList<>();
        
        for (Sentence sentence : sentences) {
            // Lấy các vocab liên quan đến sentence này
            List<SentenceVocab> sentenceVocabs = sentenceVocabRepository.findBySentenceId(sentence.getId());
            List<String> words = sentenceVocabs.stream()
                    .map(sv -> sv.getVocab().getVocab())
                    .collect(Collectors.toList());
            
            // Tạo câu đúng từ các từ đã học
            String correctAnswer = String.join(" ", words);
            
            // Tạo danh sách từ có thể chọn (bao gồm cả từ sai)
            List<String> allWords = new ArrayList<>(words);
            
            // Lấy từ vựng gây nhiễu từ bảng word (thay vì hardcode)
            List<String> distractorWords = wordRepository.findRandomDistractorWords(4);
            allWords.addAll(distractorWords);
            
            // Random thứ tự các từ trong danh sách lựa chọn
            Collections.shuffle(allWords);
            
            // Tạo signed URL cho video
            String objectName = sentence.getSentenceVideo();
            URL signedUrl = generateSignedUrl(objectName);
            String videoUrl = signedUrl != null ? signedUrl.toString() : "";
            
            SentenceBuildingQuestionDTO question = SentenceBuildingQuestionDTO.builder()
                    .id(sentence.getId())
                    .videoUrl(videoUrl)
                    .imageUrl(videoUrl)
                    .question("Ghép câu theo video:")
                    .words(allWords)
                    .correctSentence(words)
                    .correctAnswer(correctAnswer)
                    .build();
            
            questions.add(question);
        }
        
        return questions;
    }
    
    @Override
    public boolean hasSentenceBuildingForTopic(Long topicId) {
        return sentenceRepository.existsBySentenceTopicId(topicId);
    }
    
    @Override
    public List<SentenceBuildingQuestionDTO> getSentenceBuildingQuestionsForTopic(Long topicId) {
        List<Sentence> sentences = sentenceRepository.findBySentenceTopicId(topicId);
        
        List<SentenceBuildingQuestionDTO> questions = new ArrayList<>();
        
        for (Sentence sentence : sentences) {
            // Lấy các vocab liên quan đến sentence này
            List<SentenceVocab> sentenceVocabs = sentenceVocabRepository.findBySentenceId(sentence.getId());
            List<String> words = sentenceVocabs.stream()
                    .map(sv -> sv.getVocab().getVocab())
                    .collect(Collectors.toList());
            
            // Tạo câu đúng từ các từ đã học
            String correctAnswer = String.join(" ", words);
            
            // Tạo danh sách từ có thể chọn (bao gồm cả từ sai)
            List<String> allWords = new ArrayList<>(words);
            
            // Lấy từ vựng gây nhiễu từ bảng word (thay vì hardcode)
            List<String> distractorWords = wordRepository.findRandomDistractorWords(4); // Lấy 4 từ gây nhiễu
            allWords.addAll(distractorWords);
            
            // Random thứ tự các từ trong danh sách lựa chọn
            Collections.shuffle(allWords);
            
            // Tạo signed URL cho video
            String objectName = sentence.getSentenceVideo();
            URL signedUrl = generateSignedUrl(objectName);
            String videoUrl = signedUrl != null ? signedUrl.toString() : "";
            
            SentenceBuildingQuestionDTO question = SentenceBuildingQuestionDTO.builder()
                    .id(sentence.getId())
                    .videoUrl(videoUrl)
                    .imageUrl(videoUrl)
                    .question("Ghép câu theo video:")
                    .words(allWords)
                    .correctSentence(words)
                    .correctAnswer(correctAnswer)
                    .build();
            
            questions.add(question);
        }
        
        return questions;
    }
} 