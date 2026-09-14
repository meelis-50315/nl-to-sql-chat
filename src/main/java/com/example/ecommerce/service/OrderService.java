package com.example.ecommerce.service;

import com.example.ecommerce.dto.Dtos.*;
import com.example.ecommerce.entity.Customer;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.BusinessException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CustomerRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(UUID id) {
        return orderRepository.findByIdWithDetails(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByCustomer(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", customerId);
        }
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByStatus(Order.Status status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));

        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(request.shippingAddress());

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.productId()));

            if (!product.getActive()) {
                throw new BusinessException("Product is not available: " + product.getName());
            }
            if (product.getStock() < itemReq.quantity()) {
                throw new BusinessException("Insufficient stock for product: " + product.getName()
                        + " (requested: " + itemReq.quantity() + ", available: " + product.getStock() + ")");
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.quantity());
            item.setUnitPrice(product.getPrice());
            items.add(item);

            // Reduce stock
            product.setStock(product.getStock() - itemReq.quantity());
            productRepository.save(product);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity())));
        }

        order.setItems(items);
        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        log.info("Created order id={} customerId={} total={}", saved.getId(), customer.getId(), total);
        return OrderResponse.from(saved);
    }

    @Transactional
    public OrderResponse updateStatus(UUID id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        validateStatusTransition(order.getStatus(), request.status());
        order.setStatus(request.status());
        Order saved = orderRepository.save(order);
        log.info("Updated order id={} status={}", id, request.status());
        return OrderResponse.from(saved);
    }

    private void validateStatusTransition(Order.Status current, Order.Status next) {
        boolean valid = switch (current) {
            case PENDING    -> next == Order.Status.CONFIRMED || next == Order.Status.CANCELLED;
            case CONFIRMED  -> next == Order.Status.SHIPPED   || next == Order.Status.CANCELLED;
            case SHIPPED    -> next == Order.Status.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
        if (!valid) {
            throw new BusinessException("Invalid status transition: " + current + " → " + next);
        }
    }
}
