# 진행 로그

> 하루 작업 시작 전 오늘 목표를 정리하고, 끝나면 체크. 날짜별로 아래에 계속 추가.

---

## 2026-08-12

### 오늘 목표
- [X] User 엔티티 + Repository
- [X] SecurityConfig (PasswordEncoder, 로그인 URL 설정)
- [X] UserDetailsService 구현체 (User 조회 → Spring Security용 객체 변환)
- [X] 회원가입 컨트롤러/서비스 (직접 구현)
- [X] 회원가입/로그인 화면 (Thymeleaf)

---

## 2026-08-18

### 오늘 목표
- [X] Category / Product 테이블 설계 및 생성 (MySQL Workbench, SQL DDL)
- [X] Category / Product 엔티티 + Repository
- [X] 상품 목록 조회 서비스/컨트롤러 (페이징)
- [X] 상품 상세 조회 서비스/컨트롤러
- [X] 상품 목록/상세 화면 (Thymeleaf)
- [ ] 홈 화면 mock 데이터를 실제 상품 데이터로 교체 (다음 작업)

---

## 2026-08-26

### 오늘 목표
- [X] HomeController에 ProductService 주입
- [ ] "최신 상품" 조회 메서드 추가 (Repository/Service, createdAt 기준 정렬) — 현재는 정렬 조건 없이 상위 8개만 조회 중
- [X] MockProduct 내부 클래스 / mockProducts() 제거
- [ ] home.html 실제 Product 데이터 바인딩으로 수정 — category.name 바인딩은 완료, null 가드는 미반영
- [X] SecurityConfig에 /products/** permitAll 추가 (체크리스트에 없던 작업, 추가 진행)
- [X] 테스트용 상품 데이터 16건 등록 (체크리스트에 없던 작업, 추가 진행)
