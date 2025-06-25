package com.vslearn.repository;

import com.vslearn.entities.Word;
import com.vslearn.entities.Vocab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    
    @Query("SELECT w FROM Word w WHERE w.deletedAt IS NULL")
    List<Word> findAllActive();
    
    @Query("SELECT v FROM Vocab v JOIN v.subTopic st WHERE st.topic.id = :topicId AND v.deletedAt IS NULL ORDER BY FUNCTION('RAND')")
    List<Vocab> findRandomVocabByTopicId(@Param("topicId") Long topicId);
} 