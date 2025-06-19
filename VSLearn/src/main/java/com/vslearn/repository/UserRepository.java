package com.vslearn.repository;

import com.vslearn.entities.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(@Size(max = 255) @NotNull String userName);

    Optional<User> findByUserEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByUserName(String username);
    boolean existsByUserEmail(String email);

//    List<User> findByRoleIs(Integer roleId);

    List<User> findByIsActive(Boolean isActive);

//    List<User> findByCreatedAtAfter(LocalDateTime date);

//    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<User> findByCreatedBy(Long createdBy);

    List<User> findByUserNameContainingIgnoreCaseOrUserEmailContainingIgnoreCase(@Size(max = 255) @NotNull String userName, @Size(max = 255) @NotNull String userEmail);

    @Modifying
    @Query("UPDATE User u SET u.deletedAt = :deletedAt, u.deletedBy = :deletedBy WHERE u.id = :userId")
    void softDelete(Long userId, LocalDateTime deletedAt, Integer deletedBy);

    @Modifying
    @Query("UPDATE User u SET u.deletedAt = NULL, u.deletedBy = NULL WHERE u.id = :userId")
    void restore(Long userId);

    @Modifying
    @Query("UPDATE User u SET u.isActive = :isActive, u.updatedAt = :updatedAt, u.updatedBy = :updatedBy WHERE u.id = :userId")
    void updateActiveStatus(Long userId, Boolean isActive, LocalDateTime updatedAt, Integer updatedBy);

//    @Modifying
//    @Query("UPDATE User u SET u.userRole = :userRole, u.updatedAt = :updatedAt, u.updatedBy = :updatedBy WHERE u.id = :userId")
//    void updateRole(Long userId, Integer roleId, LocalDateTime updatedAt, Integer updatedBy);

    @Modifying
    @Query("UPDATE User u SET u.userAvatar = :userAvatar, u.updatedAt = :updatedAt, u.updatedBy = :updatedBy WHERE u.id = :userId")
    void updateUserAvatar(Long userId, String userAvatar, LocalDateTime updatedAt, Integer updatedBy);

    @Query("SELECT u FROM User u WHERE u.updatedAt < :date AND u.deletedAt IS NULL")
    List<User> findInactiveUsers(LocalDateTime date);

    Long countByUserRole(@Size(max = 255) @NotNull String userRole);

//    List<User> findByRoleIdAndIsActive(Integer roleId, Boolean isActive);

    Optional<User> findByActiveCode(String activeCode);
}
