package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<Order> findByStatusOrderByCreatedAtDesc(Order.Status status);

    @Query("SELECT o FROM Order o JOIN FETCH o.customer JOIN FETCH o.items i JOIN FETCH i.product WHERE o.id = :id")
    java.util.Optional<Order> findByIdWithDetails(@Param("id") UUID id);
}
