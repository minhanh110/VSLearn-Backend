package com.vslearn.repository;

import com.vslearn.entities.Vocab;
import com.vslearn.entities.VocabArea;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabAreaRepository extends JpaRepository<VocabArea, Long> {
    List<VocabArea> findByVocab(@NotNull Vocab vocab);
    List<VocabArea> findByAreaId(Long areaId);
    
    @Query("SELECT va FROM VocabArea va JOIN FETCH va.vocab v JOIN FETCH v.subTopic st WHERE st.id = :subTopicId")
    List<VocabArea> findByVocabSubTopicId(@Param("subTopicId") Long subTopicId);
    
    @Query("SELECT COUNT(va) FROM VocabArea va JOIN va.vocab v JOIN v.subTopic st WHERE st.id = :subTopicId")
    long countByVocabSubTopicId(@Param("subTopicId") Long subTopicId);
//    boolean existsByName(String name);
} 