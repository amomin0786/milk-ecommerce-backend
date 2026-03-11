package com.milk_ecommerce_backend.repository;

import com.milk_ecommerce_backend.model.Order;
import com.milk_ecommerce_backend.model.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    long countByOrderStatus(String orderStatus);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    List<Order> findByOrderStatus(String orderStatus);

    List<Order> findByOrderStatusAndOrderDateBetween(String orderStatus, Timestamp from, Timestamp to);

    List<Order> findByOrderDateBetweenOrderByOrderDateDesc(Timestamp from, Timestamp to);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.orderStatus in ('PAID','SHIPPED','DELIVERED')")
    BigDecimal totalRevenue();

    @Query("""
        SELECT COUNT(oi)
        FROM OrderItem oi
        WHERE oi.product.seller.id = :sellerId
        """)
    long countSellerOrders(@Param("sellerId") Long sellerId);

    @Query("""
        select oi
        from OrderItem oi
        where oi.product.seller.id = :sellerId
        order by oi.order.orderDate desc
        """)
    List<OrderItem> recentSellerOrders(@Param("sellerId") Long sellerId);

    @Query("""
        SELECT SUM(oi.price * oi.quantity)
        FROM OrderItem oi
        WHERE oi.product.seller.id = :sellerId
        """)
    Double totalSellerRevenue(@Param("sellerId") Long sellerId);

    @Query("SELECT o FROM Order o JOIN FETCH o.user u WHERE o.id = :id")
    Optional<Order> findByIdWithUser(@Param("id") Long id);

    @Query("""
        select coalesce(sum(o.totalAmount), 0)
        from Order o
        where o.orderStatus in ('PAID','SHIPPED','DELIVERED')
        and o.paidDate between :from and :to
        """)
    BigDecimal revenueBetween(@Param("from") Timestamp from, @Param("to") Timestamp to);

    // SELLER REPORTS
    @Query("""
        select count(oi)
        from OrderItem oi
        where oi.product.seller.id = :sellerId
          and oi.order.orderStatus = :status
          and oi.order.orderDate between :from and :to
        """)
    long countSellerOrdersByStatusBetween(
            @Param("sellerId") Long sellerId,
            @Param("status") String status,
            @Param("from") Timestamp from,
            @Param("to") Timestamp to
    );

    @Query("""
        select coalesce(sum(oi.price * oi.quantity), 0)
        from OrderItem oi
        where oi.product.seller.id = :sellerId
          and oi.order.orderStatus in ('PAID','SHIPPED','DELIVERED')
          and oi.order.orderDate between :from and :to
        """)
    BigDecimal sellerRevenueBetween(
            @Param("sellerId") Long sellerId,
            @Param("from") Timestamp from,
            @Param("to") Timestamp to
    );

    @Query("""
        select distinct o
        from Order o
        join OrderItem oi on oi.order.id = o.id
        where oi.product.seller.id = :sellerId
          and o.orderStatus = :status
          and o.orderDate between :from and :to
        order by o.orderDate desc
        """)
    List<Order> findSellerOrdersByStatusBetween(
            @Param("sellerId") Long sellerId,
            @Param("status") String status,
            @Param("from") Timestamp from,
            @Param("to") Timestamp to
    );
}