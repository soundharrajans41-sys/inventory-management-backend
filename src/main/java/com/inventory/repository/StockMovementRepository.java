package com.inventory.repository;

import com.inventory.entity.MovementType;
import com.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductIdOrderByTimestampDesc(Long productId);

    long countByProductId(Long productId);

    boolean existsByProductId(Long productId);

    List<StockMovement> findAllByOrderByTimestampDesc();

    List<StockMovement> findTop10ByOrderByTimestampDesc();

    List<StockMovement> findByMovementTypeOrderByTimestampDesc(MovementType movementType);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.timestamp >= :since ORDER BY sm.timestamp DESC")
    List<StockMovement> findRecentMovements(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(sm) FROM StockMovement sm WHERE sm.timestamp >= :since")
    long countRecentMovements(@Param("since") LocalDateTime since);

    @Query("SELECT MAX(sm.timestamp) FROM StockMovement sm WHERE sm.product.id = :productId AND sm.movementType = com.inventory.entity.MovementType.SALE")
    LocalDateTime findLastSaleTimestampByProductId(@Param("productId") Long productId);
}
