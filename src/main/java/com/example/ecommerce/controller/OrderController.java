package com.example.ecommerce.controller;

import com.example.ecommerce.dto.Dtos.*;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderResponse> findAll(
            @RequestParam(required = false) Order.Status status,
            @RequestParam(required = false) UUID customerId) {

        if (customerId != null) {
            return orderService.findByCustomer(customerId);
        }
        if (status != null) {
            return orderService.findByStatus(status);
        }
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable UUID id) {
        return orderService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(request);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable UUID id,
                                      @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(id, request);
    }
}
