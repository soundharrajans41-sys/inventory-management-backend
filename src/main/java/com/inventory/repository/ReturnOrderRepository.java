package com.inventory.repository;

import com.inventory.entity.ReturnOrder;
import com.inventory.entity.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnOrderRepository extends JpaRepository<ReturnOrder, Long> {
    Optional<ReturnOrder> findByReturnNumber(String returnNumber);
    List<ReturnOrder> findByStatus(ReturnStatus status);
    List<ReturnOrder> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(i) FROM ReturnOrderItem i WHERE i.product.id = :productId")
    long countReturnOrderItemsByProductId(@Param("productId") Long productId);
}
