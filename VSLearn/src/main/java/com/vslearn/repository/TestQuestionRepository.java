package com.vslearn.repository;

import com.vslearn.entities.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {
    
    @Query("SELECT tq FROM TestQuestion tq WHERE tq.topic.id = :topicId AND tq.deletedAt IS NULL")
    List<TestQuestion> findByTopicId(@Param("topicId") Long topicId);
    
    @Query("SELECT tq FROM TestQuestion tq WHERE tq.topic.id = :topicId AND tq.questionType = :questionType AND tq.deletedAt IS NULL")
    List<TestQuestion> findByTopicIdAndQuestionType(@Param("topicId") Long topicId, @Param("questionType") String questionType);
    
    @Query(value = "SELECT * FROM test_question WHERE topic_id = :topicId AND deleted_at IS NULL ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<TestQuestion> findRandomByTopicId(@Param("topicId") Long topicId, @Param("limit") int limit);
} 