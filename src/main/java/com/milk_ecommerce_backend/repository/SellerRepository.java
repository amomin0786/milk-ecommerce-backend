package com.milk_ecommerce_backend.repository;

import com.milk_ecommerce_backend.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    boolean existsByUser_Id(Long userId);

    Optional<Seller> findByUser_Id(Long userId);

    Optional<Seller> findByUser_Email(String email);

    long countByApprovalStatus(String approvalStatus);

    List<Seller> findByApprovalStatus(String status);

    @Query("SELECT s FROM Seller s JOIN FETCH s.user WHERE s.approvalStatus = :status")
    List<Seller> findByApprovalStatusWithUser(String status);

    @Query("SELECT s FROM Seller s JOIN FETCH s.user")
    List<Seller> findAllWithUser();

    @Query("SELECT s FROM Seller s JOIN FETCH s.user WHERE s.id = :sellerId")
    Optional<Seller> findByIdWithUser(Long sellerId);
    
//    Optional<Seller> findByUser_Id(Long userId);
}