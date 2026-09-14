package com.example.ecommerce.service;

import com.example.ecommerce.dto.Dtos.CreateCustomerRequest;
import com.example.ecommerce.dto.Dtos.CustomerResponse;
import com.example.ecommerce.entity.Customer;
import com.example.ecommerce.exception.BusinessException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        return customerRepository.findById(id)
                .map(CustomerResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new BusinessException("Customer with email already exists: " + request.email());
        }
        Customer customer = new Customer();
        customer.setEmail(request.email());
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setPhone(request.phone());
        Customer saved = customerRepository.save(customer);
        log.info("Created customer id={} email={}", saved.getId(), saved.getEmail());
        return CustomerResponse.from(saved);
    }

    @Transactional
    public CustomerResponse update(UUID id, CreateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        if (!customer.getEmail().equals(request.email()) && customerRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already in use: " + request.email());
        }
        customer.setEmail(request.email());
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setPhone(request.phone());
        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional
    public void delete(UUID id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer", id);
        }
        customerRepository.deleteById(id);
        log.info("Deleted customer id={}", id);
    }
}
