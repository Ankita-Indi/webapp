package com.example.healthcheckapi.repository;

import com.example.healthcheckapi.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository  extends JpaRepository<Product, String> {
    Optional<Product> findBySku(String sku);
    Optional<Product> findById(int id);
}
