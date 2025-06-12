package com.vslearn.repository;

import com.vslearn.entities.Vocab;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabRepository extends JpaRepository<Vocab, Long> {
    List<Vocab> findByVocab(@Size(max = 255) @NotNull String vocab);
//    List<Vocab> findByWordContainingIgnoreCase(String word);
//    boolean existsByWord(String word);
} 