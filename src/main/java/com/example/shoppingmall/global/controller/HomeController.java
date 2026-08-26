package com.example.shoppingmall.global.controller;

import com.example.shoppingmall.product.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class HomeController {
    private final ProductService productService;
    @GetMapping("/")
    public String home(Model model, @PageableDefault(size = 8) Pageable pageable) {
        model.addAttribute("products", productService.getProductList(pageable).getContent());
        return "home";
    }



}
