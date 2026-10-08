package com.inventory.repository;

import com.inventory.entity.Product;
import com.inventory.entity.StockStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByStatus(StockStatus status);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findBySupplierId(Long supplierId);

    long countByStatus(StockStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(p.quantity * p.sellingPrice), 0) FROM Product p")
    BigDecimal calculateTotalInventoryValue();

    @Query("SELECT COALESCE(SUM(p.quantity * p.purchasePrice), 0) FROM Product p")
    BigDecimal calculateTotalCostValue();

    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM Product p")
    Long calculateTotalStock();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(COALESCE(p.sku, '')) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchProducts(@Param("query") String query);

    List<Product> findAllByOrderByCreatedAtDesc();

    List<Product> findAllByOrderByIdAsc();
}
