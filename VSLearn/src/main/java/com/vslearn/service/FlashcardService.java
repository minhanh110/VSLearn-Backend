package com.vslearn.service;

import com.vslearn.dto.response.FlashcardDTO;
import com.vslearn.dto.response.PracticeQuestionDTO;
import com.vslearn.dto.response.TimelineResponseDTO;
import com.vslearn.dto.response.PracticeQuestionsResponseDTO;
import com.vslearn.dto.response.SubtopicInfoDTO;
import com.vslearn.dto.response.FlashcardProgressResponse;
import com.vslearn.dto.request.FlashcardProgressSaveRequest;
import java.util.List;

public interface FlashcardService {
    List<FlashcardDTO> getFlashcardsForSubtopic(String subtopicId);
    List<FlashcardDTO> getFlashcardsForArea(String areaId);
    long getWordCountBySubtopicId(Long subtopicId);
    TimelineResponseDTO generateTimeline(String subtopicId);
    PracticeQuestionsResponseDTO generatePracticeQuestions(String subtopicId, int start, int end);
    List<PracticeQuestionDTO> getPracticeQuestionsForSubtopic(String subtopicId);
    SubtopicInfoDTO getSubtopicInfo(String subtopicId);
    FlashcardProgressResponse saveProgress(String subtopicId, FlashcardProgressSaveRequest request);
    FlashcardProgressResponse getProgress(String subtopicId, String userId);
    
    // endpoint debug
    // Object debugSubtopicFlashcards(String subtopicId);
    // Object testVocabSubtopicMapping();
    // ResponseEntity<ResponseData<Integer>> getSubtopicWordCount(Long subtopicId);
}