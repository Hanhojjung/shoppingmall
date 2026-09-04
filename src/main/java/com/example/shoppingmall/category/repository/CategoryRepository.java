package com.example.shoppingmall.category.repository;

import com.example.shoppingmall.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    
}