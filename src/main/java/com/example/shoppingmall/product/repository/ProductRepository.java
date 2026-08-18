package com.example.shoppingmall.product.repository;

import com.example.shoppingmall.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
