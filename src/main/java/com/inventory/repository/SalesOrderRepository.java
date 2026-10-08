package com.inventory.repository;

import com.inventory.entity.SalesOrder;
import com.inventory.entity.SalesStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Optional<SalesOrder> findByOrderNumber(String orderNumber);

    List<SalesOrder> findByStatus(SalesStatus status);

    long countByStatus(SalesStatus status);

    List<SalesOrder> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM SalesOrder s WHERE s.status != 'CANCELLED'")
    BigDecimal calculateTotalSalesValue();

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM SalesOrder s WHERE s.status != 'CANCELLED' AND s.createdAt >= :since")
    BigDecimal calculateSalesValueSince(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(i.quantity * i.product.purchasePrice), 0) FROM SalesOrderItem i WHERE i.salesOrder.status != 'CANCELLED'")
    BigDecimal calculateTotalCostOfGoodsSold();

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM SalesOrderItem i WHERE i.salesOrder.status != 'CANCELLED'")
    Long calculateTotalItemsSold();

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM SalesOrderItem i WHERE i.salesOrder.status != 'CANCELLED' AND i.salesOrder.createdAt >= :since")
    Long calculateItemsSoldSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(i) FROM SalesOrderItem i WHERE i.product.id = :productId")
    long countSalesOrderItemsByProductId(@Param("productId") Long productId);

    @Query("SELECT MAX(i.salesOrder.createdAt) FROM SalesOrderItem i WHERE i.product.id = :productId AND i.salesOrder.status != com.inventory.entity.SalesStatus.CANCELLED")
    LocalDateTime findLastSalesOrderDateByProductId(@Param("productId") Long productId);
}
