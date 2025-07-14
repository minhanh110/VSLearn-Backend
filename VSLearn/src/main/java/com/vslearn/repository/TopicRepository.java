package com.vslearn.repository;

import com.vslearn.entities.Topic;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByTopicName(@Size(max = 255) @NotNull String topicName);
    
    @Query("SELECT t FROM Topic t WHERE t.deletedAt IS NULL")
    List<Topic> findByDeletedAtIsNull();
    
    Page<Topic> findByTopicNameContainingIgnoreCaseAndDeletedAtIsNull(String topicName, Pageable pageable);
    
    Page<Topic> findByDeletedAtIsNull(Pageable pageable);
    
    @Query("SELECT DISTINCT t.status FROM Topic t WHERE t.status IS NOT NULL")
    List<String> findDistinctStatuses();

    Page<Topic> findByStatusAndDeletedAtIsNull(String status, Pageable pageable);
} 