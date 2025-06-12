package com.vslearn.repository;

import com.vslearn.entities.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
//    List<Topic> findByAreaId(Long areaId);
//    List<Topic> findByNameContainingIgnoreCase(String name);
//    boolean existsByName(String name);
} 