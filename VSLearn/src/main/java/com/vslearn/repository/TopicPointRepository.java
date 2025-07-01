package com.vslearn.repository;

import com.vslearn.entities.TopicPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicPointRepository extends JpaRepository<TopicPoint, Long> {
      List<TopicPoint> findByTopicId(Long topicId);
      List<TopicPoint> findByCreatedById(Long userId);
      List<TopicPoint> findByCreatedByIdAndTopicId(Long userId, Long topicId);
//    List<TopicPoint> findByTitleContainingIgnoreCase(String title);
//    boolean existsByTitle(String title);
} 