package com.vslearn.repository;

import com.vslearn.entities.Area;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {
    List<Area> findByAreaName(@Size(max = 255) @NotNull String areaName);

    boolean existsByAreaName(@Size(max = 255) @NotNull String areaName);
} 