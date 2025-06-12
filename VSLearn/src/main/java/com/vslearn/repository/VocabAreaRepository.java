package com.vslearn.repository;

import com.vslearn.entities.Vocab;
import com.vslearn.entities.VocabArea;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabAreaRepository extends JpaRepository<VocabArea, Long> {
    List<VocabArea> findByVocab(@NotNull Vocab vocab);
    List<VocabArea> findByAreaId(Long areaId);
//    boolean existsByName(String name);
} 