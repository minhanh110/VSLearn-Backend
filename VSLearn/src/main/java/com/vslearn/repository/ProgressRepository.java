package com.vslearn.repository;

import com.vslearn.entities.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    //    List<Progress> findByUserId(Long userId);
    List<Progress> findBySubTopicId(Long subTopicId);
//    Progress findByUserIdAndSubTopicId(Long userId, Long subTopicId);
} 