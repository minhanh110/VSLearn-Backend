package com.vslearn.repository;

import com.vslearn.entities.SubTopic;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubTopicRepository extends JpaRepository<SubTopic, Long> {
    List<SubTopic> findBySubTopicName(@Size(max = 255) @NotNull String subTopicName);
    List<SubTopic> findByTopic_Id(Long topicId);
    
    // New: find children by parent id
    List<SubTopic> findByParent_Id(Long parentId);
    
    // New: find subtopic with topic info to avoid LazyInitializationException
    @Query("SELECT st FROM SubTopic st JOIN FETCH st.topic WHERE st.id = :id")
    Optional<SubTopic> findByIdWithTopic(@Param("id") Long id);
    
    // New: filter by deletedAt for soft delete support
    List<SubTopic> findByTopic_IdAndDeletedAtIsNull(Long topicId);
    List<SubTopic> findByParent_IdAndDeletedAtIsNull(Long parentId);
} 