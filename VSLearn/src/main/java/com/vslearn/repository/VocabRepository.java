package com.vslearn.repository;

import com.vslearn.entities.Vocab;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabRepository extends JpaRepository<Vocab, Long> {
    List<Vocab> findByVocab(@Size(max = 255) @NotNull String vocab);
    
    @Query("SELECT v FROM Vocab v WHERE v.deletedAt IS NULL")
    List<Vocab> findAllActive();
    
    Page<Vocab> findByVocabContainingIgnoreCaseAndDeletedAtIsNull(String vocab, Pageable pageable);
    
    Page<Vocab> findBySubTopic_Topic_TopicNameContainingIgnoreCaseAndDeletedAtIsNull(String topicName, Pageable pageable);
    
    Page<Vocab> findByDeletedAtIsNull(Pageable pageable);
    
    Page<Vocab> findByDeletedAtIsNotNull(Pageable pageable);
    
    // For region filtering - we'll need to add region field to Vocab entity later
    // For now, return all active vocab
    @Query("SELECT v FROM Vocab v WHERE v.deletedAt IS NULL")
    Page<Vocab> findByRegionContainingIgnoreCaseAndDeletedAtIsNull(String region, Pageable pageable);
} 