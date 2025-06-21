package com.vslearn.repository;

import com.vslearn.entities.Topic;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByTopicName(@Size(max = 255) @NotNull String topicName);
//    List<Topic> findByNameContainingIgnoreCase(String name);
//    boolean existsByName(String name);
} 