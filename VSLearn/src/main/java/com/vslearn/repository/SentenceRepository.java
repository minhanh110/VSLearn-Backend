package com.vslearn.repository;

import com.vslearn.entities.Sentence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentenceRepository extends JpaRepository<Sentence, Long> {
    List<Sentence> findBySentenceTopicId(Long topicId);
    boolean existsBySentenceTopicId(Long topicId);

    // New: find children by parent id
    List<Sentence> findByParent_Id(Long parentId);
} 