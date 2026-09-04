package com.example.shoppingmall.product.repository;

import com.example.shoppingmall.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategoryName(String categoryName, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    

}
