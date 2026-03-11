package com.milk_ecommerce_backend.repository;

import com.milk_ecommerce_backend.dto.AdminProductResponse;
import com.milk_ecommerce_backend.dto.PublicProductResponse;
import com.milk_ecommerce_backend.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByCreatedAtDesc();

    List<Product> findBySeller_Id(Long sellerId);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByStatus(String status);

    List<Product> findTop5BySeller_IdAndStockLessThanEqual(Long sellerId, Integer stock);

    @Query("""
        select new com.milk_ecommerce_backend.dto.PublicProductResponse(
            p.id,
            p.name,
            p.description,
            p.price,
            p.stock,
            p.imageUrl,
            p.status,
            c.id,
            c.categoryName
        )
        from Product p
        left join p.category c
        where p.status = :status
        order by p.createdAt desc
    """)
    List<PublicProductResponse> findPublicProductsByStatus(@Param("status") String status);

    @Query("""
        select new com.milk_ecommerce_backend.dto.AdminProductResponse(
            p.id,
            p.name,
            p.description,
            p.price,
            p.stock,
            p.imageUrl,
            p.status,
            c.id,
            c.categoryName,
            s.id,
            s.shopName,
            u.name,
            u.email
        )
        from Product p
        left join p.category c
        left join p.seller s
        left join s.user u
        order by p.createdAt desc
    """)
    List<AdminProductResponse> findAdminProducts();

    List<Product> findBySeller_IdOrderByCreatedAtDesc(Long sellerId);

    Optional<Product> findByIdAndSeller_Id(Long productId, Long sellerId);

    @Query("""
        select p
        from Product p
        left join fetch p.category
        left join fetch p.seller
        where p.seller.id = :sellerId
        order by p.createdAt desc
    """)
    List<Product> findBySellerIdWithRelations(@Param("sellerId") Long sellerId);

    @Query("""
        select p
        from Product p
        left join fetch p.category
        left join fetch p.seller
        where p.id = :productId and p.seller.id = :sellerId
    """)
    Optional<Product> findByIdAndSellerIdWithRelations(
            @Param("productId") Long productId,
            @Param("sellerId") Long sellerId
    );

    Page<Product> findByStatus(String status, Pageable pageable);
    long countByStatus(String status);
    Page<Product> findByStatusAndNameContainingIgnoreCase(String status, String name, Pageable pageable);
    Page<Product> findByStatusAndCategoryId(String status, Long categoryId, Pageable pageable);
    Page<Product> findByStatusAndPriceBetween(String status, Double min, Double max, Pageable pageable);
    Page<Product> findByStatusAndCategoryIdAndPriceBetween(String status, Long categoryId, Double min, Double max, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids")
    List<Product> findAllByIdForUpdate(@Param("ids") List<Long> ids);

    long countBySeller_Id(Long sellerId);
}