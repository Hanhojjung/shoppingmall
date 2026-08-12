package com.example.shoppingmall.global.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", mockProducts());
        return "home";
    }

    private List<MockProduct> mockProducts() {
        return List.of(
                new MockProduct("오버사이즈 코튼 셔츠", "패션", 39000, "https://picsum.photos/seed/shirt1/400/400"),
                new MockProduct("미니멀 크로스백", "패션", 59000, "https://picsum.photos/seed/bag1/400/400"),
                new MockProduct("무선 이어폰", "가전", 89000, "https://picsum.photos/seed/earphone1/400/400"),
                new MockProduct("아로마 디퓨저", "리빙", 32000, "https://picsum.photos/seed/diffuser1/400/400"),
                new MockProduct("러닝화", "스포츠", 79000, "https://picsum.photos/seed/shoes1/400/400"),
                new MockProduct("보습 크림 세트", "뷰티", 45000, "https://picsum.photos/seed/cream1/400/400"),
                new MockProduct("접이식 요가매트", "스포츠", 28000, "https://picsum.photos/seed/yoga1/400/400"),
                new MockProduct("무드등", "리빙", 21000, "https://picsum.photos/seed/lamp1/400/400")
        );
    }

    @Getter
    @AllArgsConstructor
    private static class MockProduct {
        private final String name;
        private final String category;
        private final int price;
        private final String imageUrl;
    }
}
