package com.example.ecommerce.dto;

import com.example.ecommerce.entity.Customer;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.Product;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class Dtos {

    // ── Customer ──────────────────────────────────────────────────────────

    @Builder
    public record CustomerResponse(
            UUID id,
            String email,
            String firstName,
            String lastName,
            String phone,
            OffsetDateTime createdAt
    ) {
        public static CustomerResponse from(Customer c) {
            return CustomerResponse.builder()
                    .id(c.getId())
                    .email(c.getEmail())
                    .firstName(c.getFirstName())
                    .lastName(c.getLastName())
                    .phone(c.getPhone())
                    .createdAt(c.getCreatedAt())
                    .build();
        }
    }

    public record CreateCustomerRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            String phone
    ) {}

    // ── Product ───────────────────────────────────────────────────────────

    @Builder
    public record ProductResponse(
            UUID id,
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            String category,
            Boolean active,
            OffsetDateTime createdAt
    ) {
        public static ProductResponse from(Product p) {
            return ProductResponse.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .description(p.getDescription())
                    .price(p.getPrice())
                    .stock(p.getStock())
                    .category(p.getCategory())
                    .active(p.getActive())
                    .createdAt(p.getCreatedAt())
                    .build();
        }
    }

    public record CreateProductRequest(
            @NotBlank @Size(max = 200) String name,
            String description,
            @NotNull @DecimalMin("0.00") BigDecimal price,
            @NotNull @Min(0) Integer stock,
            @NotBlank String category
    ) {}

    // ── Order ─────────────────────────────────────────────────────────────

    @Builder
    public record OrderResponse(
            UUID id,
            UUID customerId,
            String customerName,
            Order.Status status,
            BigDecimal totalAmount,
            String shippingAddress,
            List<OrderItemResponse> items,
            OffsetDateTime createdAt
    ) {
        public static OrderResponse from(Order o) {
            return OrderResponse.builder()
                    .id(o.getId())
                    .customerId(o.getCustomer().getId())
                    .customerName(o.getCustomer().getFirstName() + " " + o.getCustomer().getLastName())
                    .status(o.getStatus())
                    .totalAmount(o.getTotalAmount())
                    .shippingAddress(o.getShippingAddress())
                    .items(o.getItems().stream().map(OrderItemResponse::from).toList())
                    .createdAt(o.getCreatedAt())
                    .build();
        }
    }

    @Builder
    public record OrderItemResponse(
            UUID id,
            UUID productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
        public static OrderItemResponse from(OrderItem i) {
            return OrderItemResponse.builder()
                    .id(i.getId())
                    .productId(i.getProduct().getId())
                    .productName(i.getProduct().getName())
                    .quantity(i.getQuantity())
                    .unitPrice(i.getUnitPrice())
                    .lineTotal(i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .build();
        }
    }

    public record CreateOrderRequest(
            @NotNull UUID customerId,
            @NotBlank String shippingAddress,
            @NotEmpty List<OrderItemRequest> items
    ) {}

    public record OrderItemRequest(
            @NotNull UUID productId,
            @NotNull @Min(1) Integer quantity
    ) {}

    public record UpdateOrderStatusRequest(
            @NotNull Order.Status status
    ) {}
}
