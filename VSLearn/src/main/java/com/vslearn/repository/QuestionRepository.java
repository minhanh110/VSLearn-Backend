package com.vslearn.repository;

import com.vslearn.entities.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<TestQuestion, Long> {
//    List<Question> findBySubTopicId(Long subTopicId);
//    List<Question> findByQuestionTypeId(Long questionTypeId);
//    List<Question> findByQuestionContainingIgnoreCase(String question);
} 