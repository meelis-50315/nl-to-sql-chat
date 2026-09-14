package com.example.ecommerce.service;

import com.example.ecommerce.dto.Dtos.CreateProductRequest;
import com.example.ecommerce.dto.Dtos.ProductResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> findAllActive() {
        return productRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findByCategory(String category) {
        return productRepository.findByCategoryAndActiveTrue(category).stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findByPriceRange(BigDecimal min, BigDecimal max) {
        return productRepository.findByPriceRange(min, max).stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        return productRepository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(request.category());
        Product saved = productRepository.save(product);
        log.info("Created product id={} name={}", saved.getId(), saved.getName());
        return ProductResponse.from(saved);
    }

    @Transactional
    public ProductResponse update(UUID id, CreateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(request.category());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public void deactivate(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setActive(false);
        productRepository.save(product);
        log.info("Deactivated product id={}", id);
    }
}
