package com.inventory.repository;

import com.inventory.entity.PurchaseOrder;
import com.inventory.entity.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    List<PurchaseOrder> findByStatus(PurchaseStatus status);

    long countByStatus(PurchaseStatus status);

    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM PurchaseOrder p WHERE p.status = 'RECEIVED'")
    BigDecimal calculateTotalPurchasesValue();

    @Query("SELECT COUNT(i) FROM PurchaseOrderItem i WHERE i.product.id = :productId")
    long countPurchaseOrderItemsByProductId(@Param("productId") Long productId);
}
