package com.vslearn.repository;

import com.vslearn.entities.SentenceWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentenceWordRepository extends JpaRepository<SentenceWord, Long> {
    
    @Query("SELECT sw FROM SentenceWord sw WHERE sw.sentence.id = :sentenceId AND sw.deletedAt IS NULL")
    List<SentenceWord> findBySentenceId(@Param("sentenceId") Long sentenceId);
    
    @Query("SELECT sw FROM SentenceWord sw WHERE sw.word.id = :wordId AND sw.deletedAt IS NULL")
    List<SentenceWord> findByWordId(@Param("wordId") Long wordId);
    
    @Query("SELECT sw FROM SentenceWord sw WHERE sw.deletedAt IS NULL")
    List<SentenceWord> findAllActive();
} 