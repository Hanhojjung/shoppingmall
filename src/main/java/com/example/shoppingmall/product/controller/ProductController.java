package com.example.shoppingmall.product.controller;

import com.example.shoppingmall.product.entity.Product;
import com.example.shoppingmall.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String list(@PageableDefault(size = 12) Pageable pageable, Model model) {
        Page<Product> products = productService.getProductList(pageable);
        model.addAttribute("products", products);
        return "product/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductDetail(id));
        return "product/detail";
    }
}
