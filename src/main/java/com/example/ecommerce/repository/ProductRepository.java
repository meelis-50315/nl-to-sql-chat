package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByActiveTrueOrderByNameAsc();

    List<Product> findByCategoryAndActiveTrue(String category);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.price BETWEEN :min AND :max ORDER BY p.price ASC")
    List<Product> findByPriceRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stock < :threshold ORDER BY p.stock ASC")
    List<Product> findLowStock(@Param("threshold") int threshold);
}
