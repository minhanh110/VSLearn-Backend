package com.vslearn.repository;

import com.vslearn.entities.SubTopic;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTopicRepository extends JpaRepository<SubTopic, Long> {
    List<SubTopic> findBySubTopicName(@Size(max = 255) @NotNull String subTopicName);
    List<SubTopic> findByTopic_Id(Long topicId);
    
    // New: find children by parent id
    List<SubTopic> findByParent_Id(Long parentId);
} 