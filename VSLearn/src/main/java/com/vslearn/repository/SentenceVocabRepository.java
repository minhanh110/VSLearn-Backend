package com.vslearn.repository;

import com.vslearn.entities.SentenceVocab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentenceVocabRepository extends JpaRepository<SentenceVocab, Long> {
    List<SentenceVocab> findBySentenceId(Long sentenceId);
    List<SentenceVocab> findByVocabId(Long vocabId);
    void deleteBySentenceId(Long sentenceId);
} 