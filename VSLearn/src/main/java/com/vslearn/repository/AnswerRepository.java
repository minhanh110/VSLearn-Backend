package com.vslearn.repository;

import com.vslearn.entities.OptionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<OptionAnswer, Long> {
//    List<Answer> findByAnswers(Long Id);
//    List<Answer> findByTestQuestionId(Long testQuestionId);
//    List<Answer> findByUserIdAndTestQuestionId(Long userId, Long testQuestionId);
} 