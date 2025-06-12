package com.vslearn.repository;

import com.vslearn.entities.SubTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubTopicRepository extends JpaRepository<SubTopic, Long> {
//    List<SubTopic> findByTopicId(Long topicId);
//    List<SubTopic> findByNameContainingIgnoreCase(String name);
//    boolean existsByName(String name);
} 