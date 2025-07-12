package com.vslearn.repository;

import com.vslearn.entities.Vocab;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabRepository extends JpaRepository<Vocab, Long> {
    List<Vocab> findByVocab(@Size(max = 255) @NotNull String vocab);
    
    @Query("SELECT v FROM Vocab v WHERE v.deletedAt IS NULL")
    List<Vocab> findAllActive();
    
    Page<Vocab> findByVocabContainingIgnoreCaseAndDeletedAtIsNull(String vocab, Pageable pageable);
    
    Page<Vocab> findBySubTopic_Topic_TopicNameContainingIgnoreCaseAndDeletedAtIsNull(String topicName, Pageable pageable);
    
    Page<Vocab> findByDeletedAtIsNull(Pageable pageable);
    
    Page<Vocab> findByDeletedAtIsNotNull(Pageable pageable);
    
    // Comprehensive search across multiple fields
    @Query("SELECT v FROM Vocab v WHERE v.deletedAt IS NULL AND " +
           "(LOWER(v.vocab) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.subTopic.topic.topicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.subTopic.subTopicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.meaning) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Vocab> findBySearchTermContainingIgnoreCaseAndDeletedAtIsNull(String search, Pageable pageable);
    
    // Filter by region using VocabArea join
    @Query("SELECT DISTINCT v FROM Vocab v JOIN v.vocabAreas va JOIN va.area a " +
           "WHERE v.deletedAt IS NULL AND LOWER(a.areaName) LIKE LOWER(CONCAT('%', :region, '%'))")
    Page<Vocab> findByRegionContainingIgnoreCaseAndDeletedAtIsNull(String region, Pageable pageable);
    
    // Debug query to check if vocab_area has data
    @Query("SELECT COUNT(va) FROM VocabArea va")
    long countVocabAreas();
    
    // Alternative region filter using subquery
    @Query("SELECT v FROM Vocab v WHERE v.deletedAt IS NULL AND EXISTS (" +
           "SELECT 1 FROM VocabArea va JOIN va.area a " +
           "WHERE va.vocab = v AND LOWER(a.areaName) LIKE LOWER(CONCAT('%', :region, '%')))")
    Page<Vocab> findByRegionAlternative(String region, Pageable pageable);
    
    // Filter by region - handle "Toàn quốc" case
    @Query("SELECT v FROM Vocab v WHERE v.deletedAt IS NULL AND (" +
           "EXISTS (SELECT 1 FROM VocabArea va JOIN va.area a WHERE va.vocab = v AND LOWER(a.areaName) LIKE LOWER(CONCAT('%', :region, '%'))) OR " +
           "(:region = 'Toàn quốc' OR :region = 'toàn quốc' OR :region = 'TOÀN QUỐC'))")
    Page<Vocab> findByRegionWithNational(String region, Pageable pageable);
    
    // Simple region filter - for specific regions only
    @Query("SELECT DISTINCT v FROM Vocab v JOIN v.vocabAreas va JOIN va.area a " +
           "WHERE v.deletedAt IS NULL AND LOWER(a.areaName) LIKE LOWER(CONCAT('%', :region, '%'))")
    Page<Vocab> findByRegionSimple(String region, Pageable pageable);
    
    // Get vocabularies with no region assignment (Toàn quốc)
    @Query("SELECT v FROM Vocab v WHERE v.deletedAt IS NULL AND " +
           "NOT EXISTS (SELECT 1 FROM VocabArea va WHERE va.vocab = v)")
    Page<Vocab> findByNoRegionAssigned(Pageable pageable);
    
    // Filter by first letter of vocab
    @Query("SELECT v FROM Vocab v WHERE v.deletedAt IS NULL AND UPPER(SUBSTRING(v.vocab, 1, 1)) = UPPER(:letter)")
    Page<Vocab> findByFirstLetterAndDeletedAtIsNull(String letter, Pageable pageable);
    
    // Fixed combined filters query with proper logic
    @Query("SELECT DISTINCT v FROM Vocab v " +
           "LEFT JOIN v.vocabAreas va LEFT JOIN va.area a " +
           "WHERE v.deletedAt IS NULL " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(v.vocab) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(v.subTopic.topic.topicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(v.subTopic.subTopicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(v.meaning) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:topic IS NULL OR :topic = '' OR LOWER(v.subTopic.topic.topicName) LIKE LOWER(CONCAT('%', :topic, '%'))) " +
           "AND (:region IS NULL OR :region = '' OR " +
           "     (:region IN ('Toàn quốc', 'toàn quốc', 'TOÀN QUỐC') AND va.id IS NULL) OR " +
           "     (LOWER(a.areaName) LIKE LOWER(CONCAT('%', :region, '%')))) " +
           "AND (:letter IS NULL OR :letter = '' OR :letter = 'TẤT CẢ' OR UPPER(SUBSTRING(v.vocab, 1, 1)) = UPPER(:letter))")
    Page<Vocab> findByCombinedFilters(String search, String topic, String region, String letter, Pageable pageable);
    
    // Separate search-only query for better performance
    @Query("SELECT v FROM Vocab v WHERE v.deletedAt IS NULL AND " +
           "(LOWER(v.vocab) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.subTopic.topic.topicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.subTopic.subTopicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.meaning) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Vocab> findBySearchOnly(String search, Pageable pageable);
    
    // Search with topic filter
    @Query("SELECT v FROM Vocab v WHERE v.deletedAt IS NULL AND " +
           "(LOWER(v.vocab) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.subTopic.topic.topicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.subTopic.subTopicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.meaning) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "LOWER(v.subTopic.topic.topicName) LIKE LOWER(CONCAT('%', :topic, '%'))")
    Page<Vocab> findBySearchAndTopic(String search, String topic, Pageable pageable);
    
    // Search with letter filter
    @Query("SELECT v FROM Vocab v WHERE v.deletedAt IS NULL AND " +
           "(LOWER(v.vocab) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.subTopic.topic.topicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.subTopic.subTopicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.meaning) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "UPPER(SUBSTRING(v.vocab, 1, 1)) = UPPER(:letter)")
    Page<Vocab> findBySearchAndLetter(String search, String letter, Pageable pageable);
} 