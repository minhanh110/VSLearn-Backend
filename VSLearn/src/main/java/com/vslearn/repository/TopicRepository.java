package com.vslearn.repository;

import com.vslearn.entities.Topic;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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
    
    @Query("SELECT t FROM Topic t WHERE t.status = ?1 AND t.deletedAt IS NULL")
    List<Topic> findByStatusAndDeletedAtIsNull(String status);

    Page<Topic> findByCreatedByAndDeletedAtIsNull(Long createdBy, Pageable pageable);
    Page<Topic> findByStatusAndCreatedByAndDeletedAtIsNull(String status, Long createdBy, Pageable pageable);

    // New: ordered queries
    List<Topic> findByStatusAndDeletedAtIsNullOrderBySortOrderAsc(String status);
    Page<Topic> findByStatusAndDeletedAtIsNullOrderBySortOrderAsc(String status, Pageable pageable);
    List<Topic> findByDeletedAtIsNullOrderBySortOrderAsc();

    // New: homepage parents with status in list
    List<Topic> findByParentIsNullAndStatusInAndDeletedAtIsNullOrderBySortOrderAsc(Collection<String> statuses);

    // New: find children by parent id
    List<Topic> findByParent_IdAndDeletedAtIsNull(Long parentId);
    
    // New: find child topics with pending status
    List<Topic> findByParentIsNotNullAndStatusAndDeletedAtIsNull(String status);
    
    // New: find parent topics only (exclude child topics)
    Page<Topic> findByParentIsNullAndStatusAndDeletedAtIsNullOrderBySortOrderAsc(String status, Pageable pageable);
    Page<Topic> findByParentIsNullAndStatusAndDeletedAtIsNull(String status, Pageable pageable);
    Page<Topic> findByParentIsNullAndTopicNameContainingIgnoreCaseAndDeletedAtIsNull(String topicName, Pageable pageable);
    Page<Topic> findByParentIsNullAndDeletedAtIsNull(Pageable pageable);
    
    // New: find parent topics only (List version for vocab service)
    List<Topic> findByParentIsNullAndStatusAndDeletedAtIsNull(String status);
} 