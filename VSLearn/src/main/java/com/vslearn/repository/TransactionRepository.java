package com.vslearn.repository;
import com.vslearn.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
//    List<Transaction> findByUserId(Long userId);
//    List<Transaction> findByPackageId(Long packageId);
//    List<Transaction> findByUserIdAndPackageId(Long userId, Long packageId);
} 