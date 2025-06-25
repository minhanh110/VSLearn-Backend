package com.vslearn.service.impl;

import com.vslearn.dto.request.TestSubmissionRequest;
import com.vslearn.dto.response.ResponseData;
import com.vslearn.dto.response.TestQuestionResponseDTO;
import com.vslearn.dto.response.TestSubmissionResponseDTO;
import com.vslearn.entities.*;
import com.vslearn.repository.*;
import com.vslearn.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.io.FileInputStream;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.auth.oauth2.GoogleCredentials;

@Service
public class TestServiceImpl implements TestService {

    private final TestQuestionRepository testQuestionRepository;
    private final WordRepository wordRepository;
    private final TopicRepository topicRepository;
    private final SubTopicRepository subTopicRepository;
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final VocabRepository vocabRepository;
    private final VocabAreaRepository vocabAreaRepository;

    private final Storage storage;
    private final String bucketName;

    public TestServiceImpl(
            TestQuestionRepository testQuestionRepository,
            WordRepository wordRepository,
            TopicRepository topicRepository,
            SubTopicRepository subTopicRepository,
            ProgressRepository progressRepository,
            UserRepository userRepository,
            VocabRepository vocabRepository,
            VocabAreaRepository vocabAreaRepository,
            @Value("${gcp.storage.credentials.location}") String credentialsPath,
            @Value("${gcp.storage.bucket.name}") String bucketName
    ) throws IOException {
        System.out.println("=== TestServiceImpl constructor called ===");
        System.out.println("Credentials path: " + credentialsPath);
        System.out.println("Bucket name: " + bucketName);
        
        this.testQuestionRepository = testQuestionRepository;
        this.wordRepository = wordRepository;
        this.topicRepository = topicRepository;
        this.subTopicRepository = subTopicRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.vocabRepository = vocabRepository;
        this.vocabAreaRepository = vocabAreaRepository;
        this.bucketName = bucketName;
        
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath.replace("file:", "")));
            this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            System.out.println("✅ Google Cloud Storage initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Error initializing Google Cloud Storage: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public ResponseEntity<ResponseData<List<TestQuestionResponseDTO>>> generateTest(Long userId, Long topicId) {
        try {
            List<TestQuestionResponseDTO> questions = generateTestLogic(userId, topicId);
            return ResponseEntity.ok(ResponseData.<List<TestQuestionResponseDTO>>builder()
                .status(200)
                .message("Test generated successfully")
                .data(questions)
                .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ResponseData.<List<TestQuestionResponseDTO>>builder()
                .status(400)
                .message("Failed to generate test: " + e.getMessage())
                .data(null)
                .build());
        }
    }

    @Override
    public ResponseEntity<ResponseData<TestSubmissionResponseDTO>> submitTest(TestSubmissionRequest request) {
        try {
            TestSubmissionResponseDTO result = submitTestLogic(request);
            return ResponseEntity.ok(ResponseData.<TestSubmissionResponseDTO>builder()
                .status(200)
                .message("Test submitted successfully")
                .data(result)
                .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ResponseData.<TestSubmissionResponseDTO>builder()
                .status(400)
                .message("Failed to submit test: " + e.getMessage())
                .data(null)
                .build());
        }
    }

    @Override
    public ResponseEntity<ResponseData<TestSubmissionResponseDTO.NextTopicInfo>> getNextTopic(Long userId, Long currentTopicId) {
        try {
            TestSubmissionResponseDTO.NextTopicInfo nextTopic = getNextTopicLogic(userId, currentTopicId);
            return ResponseEntity.ok(ResponseData.<TestSubmissionResponseDTO.NextTopicInfo>builder()
                .status(200)
                .message("Next topic retrieved successfully")
                .data(nextTopic)
                .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ResponseData.<TestSubmissionResponseDTO.NextTopicInfo>builder()
                .status(400)
                .message("Failed to get next topic: " + e.getMessage())
                .data(null)
                .build());
        }
    }

    @Override
    public ResponseEntity<ResponseData<TestSubmissionResponseDTO.NextSubtopicInfo>> getNextSubtopic(Long topicId) {
        try {
            TestSubmissionResponseDTO.NextSubtopicInfo nextSubtopic = getNextSubtopicLogic(topicId);
            return ResponseEntity.ok(ResponseData.<TestSubmissionResponseDTO.NextSubtopicInfo>builder()
                .status(200)
                .message("Next subtopic retrieved successfully")
                .data(nextSubtopic)
                .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ResponseData.<TestSubmissionResponseDTO.NextSubtopicInfo>builder()
                .status(400)
                .message("Failed to get next subtopic: " + e.getMessage())
                .data(null)
                .build());
        }
    }

    @Override
    public ResponseEntity<ResponseData<String>> getTopicName(Long topicId) {
        try {
            String topicName = getTopicNameLogic(topicId);
            return ResponseEntity.ok(ResponseData.<String>builder()
                .status(200)
                .message("Topic name retrieved successfully")
                .data(topicName)
                .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ResponseData.<String>builder()
                .status(400)
                .message("Failed to get topic name: " + e.getMessage())
                .data(null)
                .build());
        }
    }

    private List<TestQuestionResponseDTO> generateTestLogic(Long userId, Long topicId) {
        System.out.println("=== generateTestLogic started for topicId: " + topicId + " ===");
        List<TestQuestionResponseDTO> testQuestions = new ArrayList<>();
        
        // Get existing test questions for this topic
        List<TestQuestion> existingQuestions = testQuestionRepository.findByTopicId(topicId);
        System.out.println("Found " + existingQuestions.size() + " existing test questions");
        
        // If we have enough existing questions, use them
        if (existingQuestions.size() >= 20) {
            System.out.println("Using existing questions");
            // Randomly select 20 questions
            Collections.shuffle(existingQuestions);
            existingQuestions = existingQuestions.subList(0, 20);
            
            for (TestQuestion question : existingQuestions) {
                testQuestions.add(convertToDTO(question));
            }
        } else {
            System.out.println("Generating questions from topic vocab");
            // Generate questions from topic vocab
            List<Vocab> topicVocabs = wordRepository.findRandomVocabByTopicId(topicId);
            System.out.println("Found " + topicVocabs.size() + " vocab for topic " + topicId);
            
            // Limit to 20 vocab if more than 20
            if (topicVocabs.size() > 20) {
                topicVocabs = topicVocabs.subList(0, 20);
            }
            
            if (topicVocabs.size() < 20) {
                System.out.println("Not enough vocab, getting more from all vocab");
                // If not enough vocab, get more from all vocab
                List<Vocab> allVocabs = vocabRepository.findAllActive();
                System.out.println("Total active vocab: " + allVocabs.size());
                Collections.shuffle(allVocabs);
                int needed = 20 - topicVocabs.size();
                topicVocabs.addAll(allVocabs.subList(0, Math.min(needed, allVocabs.size())));
                System.out.println("After adding more vocab: " + topicVocabs.size() + " total");
            }
            
            // Generate 20 questions with different types
            int questionsPerType = 7; // 7 multiple choice, 7 true/false, 6 essay = 20 total
            
            // Multiple choice questions
            for (int i = 0; i < Math.min(questionsPerType, topicVocabs.size()); i++) {
                Vocab vocab = topicVocabs.get(i);
                testQuestions.add(generateMultipleChoiceQuestion(vocab, topicVocabs, i));
            }
            
            // True/False questions
            for (int i = 0; i < Math.min(questionsPerType, topicVocabs.size() - questionsPerType); i++) {
                Vocab vocab = topicVocabs.get(i + questionsPerType);
                testQuestions.add(generateTrueFalseQuestion(vocab, i));
            }
            
            // Essay questions
            for (int i = 0; i < Math.min(6, topicVocabs.size() - 2 * questionsPerType); i++) {
                Vocab vocab = topicVocabs.get(i + 2 * questionsPerType);
                testQuestions.add(generateEssayQuestion(vocab, i));
            }
            
            // Shuffle the questions
            Collections.shuffle(testQuestions);
        }
        
        System.out.println("Generated " + testQuestions.size() + " test questions");
        return testQuestions;
    }

    private TestSubmissionResponseDTO submitTestLogic(TestSubmissionRequest request) {
        // Calculate score
        int correctAnswers = 0;
        int totalQuestions = request.getAnswers().size();
        
        for (TestSubmissionRequest.TestAnswer answer : request.getAnswers()) {
            // For now, we'll assume all answers are correct if they're not empty
            // In a real implementation, you'd validate against correct answers
            if (answer.getAnswer() != null && !answer.getAnswer().trim().isEmpty()) {
                correctAnswers++;
            }
        }
        
        int score = (int) Math.round((double) correctAnswers / totalQuestions * 100);
        boolean isPassed = score >= 90;
        
        // If passed, mark topic as completed
        boolean topicCompleted = false;
        if (isPassed) {
            topicCompleted = markTopicAsCompleted(request.getUserId(), request.getTopicId());
        }
        
        // Get next topic info
        TestSubmissionResponseDTO.NextTopicInfo nextTopic = getNextTopicLogic(request.getUserId(), request.getTopicId());
        
        return new TestSubmissionResponseDTO(
            totalQuestions,
            correctAnswers,
            score,
            isPassed,
            topicCompleted,
            nextTopic
        );
    }

    private TestSubmissionResponseDTO.NextTopicInfo getNextTopicLogic(Long userId, Long currentTopicId) {
        // Get all topics ordered by ID
        List<Topic> allTopics = topicRepository.findAll().stream()
            .filter(topic -> topic.getDeletedAt() == null)
            .sorted(Comparator.comparing(Topic::getId))
            .collect(Collectors.toList());
        
        // Find current topic index
        int currentIndex = -1;
        for (int i = 0; i < allTopics.size(); i++) {
            if (allTopics.get(i).getId().equals(currentTopicId)) {
                currentIndex = i;
                break;
            }
        }
        
        // If current topic not found or it's the last one, return null
        if (currentIndex == -1 || currentIndex == allTopics.size() - 1) {
            return new TestSubmissionResponseDTO.NextTopicInfo(null, null, false);
        }
        
        // Return next topic
        Topic nextTopic = allTopics.get(currentIndex + 1);
        return new TestSubmissionResponseDTO.NextTopicInfo(
            nextTopic.getId(),
            nextTopic.getTopicName(),
            true
        );
    }

    private TestQuestionResponseDTO convertToDTO(TestQuestion question) {
        TestQuestionResponseDTO dto = new TestQuestionResponseDTO();
        dto.setId(question.getId());
        dto.setType(question.getQuestionType());
        dto.setQuestion(question.getQuestionContent());
        dto.setCorrectAnswer(question.getQuestionAnswer().getVocab());
        
        // Set default values for missing fields
        dto.setVideoUrl("/videos/sign-language-demo.mp4");
        dto.setImageUrl("/placeholder.svg?height=300&width=300");
        
        // Generate options for multiple choice
        if ("multiple-choice".equals(question.getQuestionType())) {
            List<String> options = Arrays.asList(
                question.getQuestionAnswer().getVocab(),
                "Option 2",
                "Option 3",
                "Option 4"
            );
            Collections.shuffle(options);
            dto.setOptions(options);
        }
        
        return dto;
    }

    private TestQuestionResponseDTO generateMultipleChoiceQuestion(Vocab vocab, List<Vocab> allVocabs, int index) {
        TestQuestionResponseDTO dto = new TestQuestionResponseDTO();
        dto.setId((long) (index + 1));
        dto.setType("multiple-choice");
        
        // Get video URL from vocab_area
        String videoUrl = getVideoUrlForVocab(vocab);
        dto.setVideoUrl(videoUrl);
        dto.setImageUrl(videoUrl); // Use same URL for image
        
        dto.setQuestion("HÀNH ĐỘNG NÀY CÓ NGHĨA LÀ GÌ?");
        dto.setCorrectAnswer(vocab.getVocab());
        
        // Generate 4 options including the correct answer
        List<String> options = new ArrayList<>();
        options.add(vocab.getVocab());
        
        // Add 3 random wrong options
        List<Vocab> wrongOptions = allVocabs.stream()
            .filter(w -> !w.getVocab().equals(vocab.getVocab()))
            .collect(Collectors.toList());
        Collections.shuffle(wrongOptions);
        
        for (int i = 0; i < Math.min(3, wrongOptions.size()); i++) {
            options.add(wrongOptions.get(i).getVocab());
        }
        
        // If we don't have enough wrong options, add generic ones
        while (options.size() < 4) {
            options.add("Option " + (options.size() + 1));
        }
        
        Collections.shuffle(options);
        dto.setOptions(options);
        
        return dto;
    }

    private TestQuestionResponseDTO generateTrueFalseQuestion(Vocab vocab, int index) {
        TestQuestionResponseDTO dto = new TestQuestionResponseDTO();
        dto.setId((long) (index + 8)); // Start from 8 to avoid conflicts
        dto.setType("true-false");
        
        // Get video URL from vocab_area
        String videoUrl = getVideoUrlForVocab(vocab);
        dto.setVideoUrl(videoUrl);
        dto.setImageUrl(videoUrl); // Use same URL for image
        
        // Randomly decide if the statement is true or false
        boolean isTrue = Math.random() < 0.5; // 50% chance for true/false
        
        if (isTrue) {
            // True statement: "HÀNH ĐỘNG NÀY CÓ NGHĨA LÀ: [vocab]?"
            dto.setQuestion("HÀNH ĐỘNG NÀY CÓ NGHĨA LÀ: " + vocab.getVocab() + "?");
            dto.setCorrectAnswer("true");
            dto.setTrueFalseAnswer(true);
        } else {
            // False statement: use a random wrong vocab
            List<Vocab> allVocabs = vocabRepository.findAllActive();
            List<Vocab> wrongVocabs = allVocabs.stream()
                .filter(v -> !v.getVocab().equals(vocab.getVocab()))
                .collect(Collectors.toList());
            
            if (!wrongVocabs.isEmpty()) {
                Collections.shuffle(wrongVocabs);
                String wrongVocab = wrongVocabs.get(0).getVocab();
                dto.setQuestion("HÀNH ĐỘNG NÀY CÓ NGHĨA LÀ: " + wrongVocab + "?");
                dto.setCorrectAnswer("false");
                dto.setTrueFalseAnswer(false);
            } else {
                // Fallback to true statement if no wrong vocab available
                dto.setQuestion("HÀNH ĐỘNG NÀY CÓ NGHĨA LÀ: " + vocab.getVocab() + "?");
                dto.setCorrectAnswer("true");
                dto.setTrueFalseAnswer(true);
            }
        }
        
        return dto;
    }

    private TestQuestionResponseDTO generateEssayQuestion(Vocab vocab, int index) {
        TestQuestionResponseDTO dto = new TestQuestionResponseDTO();
        dto.setId((long) (index + 15)); // Start from 15 to avoid conflicts
        dto.setType("essay");
        
        // Get video URL from vocab_area
        String videoUrl = getVideoUrlForVocab(vocab);
        dto.setVideoUrl(videoUrl);
        dto.setImageUrl(videoUrl); // Use same URL for image
        
        dto.setQuestion("HÀNH ĐỘNG NÀY CÓ NGHĨA LÀ GÌ?");
        dto.setCorrectAnswer(vocab.getVocab());
        dto.setEssayPrompt("HÃY ĐIỀN ĐÁP ÁN CỦA BẠN VÀO ĐÂY");
        
        return dto;
    }

    private String getVideoUrlForVocab(Vocab vocab) {
        System.out.println("=== getVideoUrlForVocab called for vocab: " + vocab.getVocab() + " ===");
        try {
            // Try to get video URL from vocab_area
            List<VocabArea> vocabAreas = vocabAreaRepository.findByVocab(vocab);
            System.out.println("Found " + vocabAreas.size() + " vocab areas for vocab: " + vocab.getVocab());
            
            if (!vocabAreas.isEmpty()) {
                String gifName = vocabAreas.get(0).getVocabAreaGif();
                System.out.println("GIF name: " + gifName);
                
                if (gifName != null && !gifName.isEmpty()) {
                    // If it's a full URL, return as is
                    if (gifName.startsWith("http")) {
                        System.out.println("Returning full URL: " + gifName);
                        return gifName;
                    }
                    // If it's a filename, generate signed URL from Google Cloud Storage
                    URL signedUrl = generateSignedUrl(gifName);
                    String result = signedUrl != null ? signedUrl.toString() : null;
                    System.out.println("Generated signed URL: " + result);
                    return result;
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting video URL for vocab " + vocab.getVocab() + ": " + e.getMessage());
            e.printStackTrace();
        }
        // Fallback to placeholder
        System.out.println("Using fallback URL: /videos/sign-language-demo.mp4");
        return "/videos/sign-language-demo.mp4";
    }

    private URL generateSignedUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }
        try {
            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            return storage.signUrl(blobInfo, 15, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean markTopicAsCompleted(Long userId, Long topicId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return false;
            
            // Get all subtopics for this topic
            List<SubTopic> subTopics = subTopicRepository.findByTopic_Id(topicId);
            
            // Mark all subtopics as completed
            for (SubTopic subTopic : subTopics) {
                Progress progress = Progress.builder()
                    .subTopic(subTopic)
                    .createdBy(user)
                    .isComplete(true)
                    .createdAt(Instant.now())
                    .build();
                
                progressRepository.save(progress);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private TestSubmissionResponseDTO.NextSubtopicInfo getNextSubtopicLogic(Long topicId) {
        try {
            // Get all subtopics for this topic, ordered by ID
            List<SubTopic> subTopics = subTopicRepository.findByTopic_Id(topicId);
            
            if (subTopics.isEmpty()) {
                return new TestSubmissionResponseDTO.NextSubtopicInfo(null, null, null, null, false);
            }
            
            // Get the first subtopic (ordered by ID)
            SubTopic firstSubtopic = subTopics.stream()
                .sorted(Comparator.comparing(SubTopic::getId))
                .findFirst()
                .orElse(null);
            
            if (firstSubtopic == null) {
                return new TestSubmissionResponseDTO.NextSubtopicInfo(null, null, null, null, false);
            }
            
            return new TestSubmissionResponseDTO.NextSubtopicInfo(
                firstSubtopic.getId(),
                firstSubtopic.getSubTopicName(),
                firstSubtopic.getTopic().getId(),
                firstSubtopic.getTopic().getTopicName(),
                true
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new TestSubmissionResponseDTO.NextSubtopicInfo(null, null, null, null, false);
        }
    }

    private String getTopicNameLogic(Long topicId) {
        try {
            Topic topic = topicRepository.findById(topicId).orElse(null);
            if (topic != null) {
                return topic.getTopicName();
            }
            return "Chủ đề không xác định";
        } catch (Exception e) {
            e.printStackTrace();
            return "Chủ đề không xác định";
        }
    }
} 