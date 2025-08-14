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
    
    // Filter by status
    @Query("SELECT v FROM Vocab v WHERE v.deletedAt IS NULL AND v.status = :status")
    Page<Vocab> findByStatusAndDeletedAtIsNull(String status, Pageable pageable);
    
    // Search with status filter
    @Query("SELECT v FROM Vocab v WHERE v.deletedAt IS NULL AND " +
           "(LOWER(v.vocab) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.subTopic.topic.topicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.subTopic.subTopicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.meaning) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "v.status = :status")
    Page<Vocab> findBySearchAndStatus(String search, String status, Pageable pageable);
    
    // Search with topic and status filter
    @Query("SELECT v FROM Vocab v WHERE v.deletedAt IS NULL AND " +
           "(LOWER(v.vocab) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.subTopic.topic.topicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.subTopic.subTopicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.meaning) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "LOWER(v.subTopic.topic.topicName) LIKE LOWER(CONCAT('%', :topic, '%')) AND " +
           "v.status = :status")
    Page<Vocab> findBySearchAndTopicAndStatus(String search, String topic, String status, Pageable pageable);
    
    // Search with letter and status filter
    @Query("SELECT v FROM Vocab v WHERE v.deletedAt IS NULL AND " +
           "(LOWER(v.vocab) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.subTopic.topic.topicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.subTopic.subTopicName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.meaning) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "UPPER(SUBSTRING(v.vocab, 1, 1)) = UPPER(:letter) AND " +
           "v.status = :status")
    Page<Vocab> findBySearchAndLetterAndStatus(String search, String letter, String status, Pageable pageable);
    
    // Combined filters with status
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
           "AND (:letter IS NULL OR :letter = '' OR :letter = 'TẤT CẢ' OR UPPER(SUBSTRING(v.vocab, 1, 1)) = UPPER(:letter)) " +
           "AND (:status IS NULL OR :status = '' OR v.status = :status)")
    Page<Vocab> findByCombinedFiltersWithStatus(String search, String topic, String region, String letter, String status, Pageable pageable);

    Page<Vocab> findByCreatedByAndDeletedAtIsNull(Long createdBy, Pageable pageable);
    Page<Vocab> findByStatusAndCreatedByAndDeletedAtIsNull(String status, Long createdBy, Pageable pageable);
    List<Vocab> findBySubTopic_Id(Long subTopicId);
    long countBySubTopic_Id(Long subTopicId);
    
    // ========== CAMERA SIGN METHODS ==========
    
    // Find by status only (for camera sign)
    Page<Vocab> findByStatus(String status, Pageable pageable);
    List<Vocab> findByStatus(String status);
    
    // Find by vocab areas area name and status
    @Query("SELECT DISTINCT v FROM Vocab v JOIN v.vocabAreas va JOIN va.area a " +
           "WHERE v.status = :status AND LOWER(a.areaName) = LOWER(:areaName)")
    List<Vocab> findByVocabAreas_Area_AreaNameAndStatus(String areaName, String status);
    
    // Find by sub topic sort order and status
    @Query("SELECT v FROM Vocab v WHERE v.status = :status AND v.subTopic.sortOrder <= :sortOrder")
    List<Vocab> findBySubTopic_SortOrderLessThanEqualAndStatus(Long sortOrder, String status);
    
    @Query("SELECT v FROM Vocab v WHERE v.status = :status AND v.subTopic.sortOrder BETWEEN :minSortOrder AND :maxSortOrder")
    List<Vocab> findBySubTopic_SortOrderBetweenAndStatus(Long minSortOrder, Long maxSortOrder, String status);
    
    @Query("SELECT v FROM Vocab v WHERE v.status = :status AND v.subTopic.sortOrder > :sortOrder")
    List<Vocab> findBySubTopic_SortOrderGreaterThanAndStatus(Long sortOrder, String status);
    
    // Find by vocab containing and status
    @Query("SELECT v FROM Vocab v WHERE v.status = :status AND LOWER(v.vocab) LIKE LOWER(CONCAT('%', :vocab, '%'))")
    List<Vocab> findByVocabContainingIgnoreCaseAndStatus(String vocab, String status);
    
    // New: filter by deletedAt for soft delete support
    List<Vocab> findBySubTopic_IdAndDeletedAtIsNull(Long subTopicId);
} 