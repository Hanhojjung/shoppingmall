# 학습 노트

> 코딩하면서 물어본 내용, 헷갈렸던 개념을 여기에 정리. 나중에 면접/포트폴리오 설명할 때 다시 훑어보는 용도.

---

## Spring / JPA

**`@NoArgsConstructor(access = AccessLevel.PROTECTED)`**
JPA는 DB row를 엔티티 객체로 바꿀 때 리플렉션으로 기본 생성자를 호출한 뒤 필드를 채운다. 그래서 파라미터 없는 생성자가 필수. `public`으로 열면 아무 데서나 빈 엉터리 객체를 만들 수 있고, `private`이면 JPA 프록시 생성(지연 로딩 등)이 깨진다. `protected`가 절충안: JPA는 접근 가능하고, 외부 코드는 `new Product()`로 못 만들게 막는다. 대신 `@Builder`로만 제대로 채운 객체를 생성하도록 유도.

**`@Getter`만 쓰고 `@Setter`는 안 쓰는 이유**
`@Setter`를 열어두면 어디서든 `product.setPrice(0)`처럼 필드를 무분별하게 바꿀 수 있어 객체 상태가 언제 왜 바뀌는지 추적하기 어려워진다. 대신 `updateProfile()`, `changePassword()`처럼 의도가 드러나는 메서드만 열어주면, 변경 가능한 지점이 명확해지고 그 안에서 유효성 검증도 넣을 수 있다.

**Entity vs Repository**
Entity = 테이블과 매핑되는 클래스(필드 구조). Repository = 그 Entity에 대한 DB 접근(조회/저장/삭제) 인터페이스. `JpaRepository<User, Long>`만 상속하면 기본 CRUD는 자동 구현됨.

**`@Id` / `@GeneratedValue(strategy = GenerationType.IDENTITY)`**
`@Id`는 해당 필드가 PK임을 JPA에 알려줌. `@GeneratedValue(IDENTITY)`는 그 PK 채번을 DB의 `AUTO_INCREMENT`에 위임한다는 뜻(MySQL DDL의 `AUTO_INCREMENT`와 짝을 이룸). 다른 전략(`SEQUENCE`, `AUTO`)도 있지만 MySQL + AUTO_INCREMENT 조합에서는 `IDENTITY`가 표준.

- 조회(SELECT) 시: Hibernate가 빈 객체를 만든 뒤 DB에 이미 있던 id 값을 그대로 채워 넣음 → id 증가 없음
- 저장(INSERT) 시: id가 없는 새 객체를 DB에 넣을 때 MySQL이 AUTO_INCREMENT로 새 id를 채번 → id 증가 발생

**`repository.save()`가 INSERT/UPDATE를 구분하는 방식**
`save()` 내부에서 엔티티의 id 필드를 보고 판단함.
- id가 `null` → 아직 DB에 없는 새 객체로 간주 → `INSERT` 실행 (PK 채번 O)
- id가 값이 있음 → 이미 DB에 있는 객체로 간주 → `UPDATE` 실행 (PK 채번 X, 기존 row 수정)

조회는 애초에 `findById()`/`findAll()` 같은 별도 메서드를 호출하는 것이므로 구분이 필요 없음(처음부터 SELECT 전용).

**`@Builder`의 역할**
Lombok이 빌더 패턴을 자동 생성해주는 애노테이션. 생성자에 붙이면 `Category.builder().name("패션").build()`처럼 필드 이름으로 값을 하나씩 채워서 객체를 만들 수 있게 해줌.

- 가독성: `new Category("패션")`처럼 순서만으로 값을 넘기면 파라미터가 많을 때 헷갈리는데, `.name("패션")`처럼 이름이 붙어서 명확함
- 선택적 조합: 생성자는 정의된 값을 순서대로 다 넣어야 하지만, 빌더는 필요한 필드만 골라 체이닝 가능
- 의도 있는 생성 강제: `@Setter`를 열어두는 대신 생성 시점에만 값을 채우고 이후엔 의도가 담긴 메서드로만 상태를 바꾸도록 설계할 수 있음 (→ [[`@Setter` 안 쓰는 이유]]와 같은 맥락)

이 프로젝트에서는 `User`, `Product`, `Category` 모두 `@NoArgsConstructor(PROTECTED)` + `@Builder` 조합으로 통일해서 "무분별한 setter 노출 금지, 생성/변경 지점 명확화" 원칙을 지키고 있음.

**JPA(애노테이션 매핑) vs MyBatis(SQL 직접 매핑) 비교**
실무(레거시 Spring + MyBatis)에서는 아래처럼 SQL을 직접 짜고 `resultMap`으로 컬럼-필드를 수동 매핑함.
```xml
<select id="findProductWithCategory" resultMap="productResultMap">
    SELECT p.*, c.name as category_name
    FROM product p JOIN category c ON p.category_id = c.id
    WHERE p.id = #{id}
</select>
```
JPA에서는 연관관계 애노테이션 하나가 이 JOIN 쿼리 + 매핑을 대신함.
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id")
private Category category;
```
`product.getCategory().getName()`을 호출하는 시점에 Hibernate가 필요한 SELECT를 알아서 실행함.

- **MyBatis 장점**: SQL이 코드에 그대로 보여서 언제 어떤 쿼리가 나가는지 예측 가능, 복잡한 쿼리 튜닝이 쉬움
- **JPA 장점**: 생산성 높음(반복적인 CRUD/매핑 코드 불필요), 객체 그래프 탐색이 자연스러움
- **JPA 단점/주의점**: 지연 로딩(`LAZY`) 때문에 "언제 실제 쿼리가 나가는지"가 코드만 봐서는 안 보임. Spring Boot는 기본적으로 **OSIV(Open Session In View)**가 켜져 있어서, 컨트롤러 로직이 끝나고 트랜잭션이 종료된 뒤에도 뷰(Thymeleaf) 렌더링 중에 지연 로딩된 연관관계(`product.category.name` 등)에 접근 가능함 — 편하지만 뷰 렌더링 중 쿼리가 터지는 걸 놓치기 쉬워 실무에서는 끄는 경우도 많음
- 실무에서는 단순 CRUD는 JPA, 복잡한 통계/리포트성 쿼리는 QueryDSL이나 네이티브 쿼리로 섞어 쓰는 하이브리드 방식이 흔함

---

## MySQL

**INT vs BIGINT**
INT는 약 -21억~21억 범위(4바이트), BIGINT는 그보다 훨씬 큰 범위(8바이트). PK처럼 계속 증가하며 장기간 쌓이는 컬럼은 BIGINT를 쓰는 게 안전한 관례. (지금 프로젝트의 `user.id`는 DB에 INT로 만들어져 있고 Java에서는 Long으로 매핑 중 — 당장 문제는 없지만 나중에 `ALTER TABLE user MODIFY id BIGINT;`로 맞춰도 됨)

**ENUM 사용법**
`ENUM('A', 'B', 'C')`로 컬럼에 허용값을 DB 레벨에서 제한. Java에서는 `@Enumerated(EnumType.STRING)`으로 매핑해서 enum 이름 문자열 그대로 저장(숫자 인덱스로 저장하는 `ORDINAL`은 순서 바뀌면 데이터가 깨지므로 지양).

**FK(외래키) 관계**
`product.category_id`는 `category.id`를 참조하는 FK. `CONSTRAINT ... FOREIGN KEY (category_id) REFERENCES category(id)`로 선언하면, 존재하지 않는 카테고리를 가리킬 수 없고 참조 중인 카테고리를 함부로 지울 수도 없게 DB가 막아준다. `Category(1) : Product(N)` 관계.

**로그인 구조**
`id`는 자동 증가하는 내부 식별자(PK)일 뿐, 실제 로그인은 `email` + `password`로 이루어진다. `id`는 사용자에게 노출되거나 입력받는 값이 아니라 DB 내부 참조용.

---

## Spring Security

**`UserDetailsService.loadUserByUsername()`가 하는 일 / 안 하는 일**
이 메서드는 "이메일로 유저를 찾아서 Spring Security가 이해할 수 있는 형태(`UserDetails`)로 변환"만 한다. 비밀번호가 맞는지 비교하는 로직은 여기 없음. 실제 비교는 `DaoAuthenticationProvider`가 반환된 `UserDetails.getPassword()`(DB의 암호화된 비밀번호)와 사용자가 입력한 원문 비밀번호를 `PasswordEncoder.matches()`로 비교하며 수행한다.
이 프로젝트: `CustomUserDetailsService.loadUserByUsername()` → 이메일로 `User` 조회 → `CustomUserDetails`로 감싸서 반환.

**`UserDetails` 구현체가 담고 있는 정보**
`CustomUserDetails`는 단순 DTO가 아니라 Spring Security가 인증/인가 판단에 쓰는 정보 묶음이다.
- `getPassword()`: DB의 암호화된 비밀번호 → 비교용
- `getAuthorities()`: 권한 목록(`ROLE_XXX` 등) → 인가(authorization) 판단용
- `isEnabled()` / `isAccountNonLocked()` 등: 계정 상태 → 로그인 자체를 막을지 여부

이 프로젝트는 `UserStatus.SUSPENDED`면 `isAccountNonLocked() = false`, `UserStatus.ACTIVE`일 때만 `isEnabled() = true`로 구현해서, 회원 상태(정지/활성)로 로그인 가능 여부를 제어하고 있음.

**`CustomUserDetailsService`를 어떻게 자동으로 찾아 쓰는가 (명시적 연결 코드 없음)**
`SecurityConfig`에는 `CustomUserDetailsService`를 직접 지정하는 코드가 없다. 컨테이너 안에 `UserDetailsService` 빈이 1개, `PasswordEncoder` 빈이 1개만 있으면, Spring Security가 내부적으로 `DaoAuthenticationProvider`를 자동 구성해서 이 둘을 엮어준다. 구현체가 2개 이상이거나 커스텀 인증 방식이 필요해지면 그때 `AuthenticationManagerBuilder`/`AuthenticationProvider` 빈을 명시적으로 등록해야 함.

**로그인 화면 전체 흐름 — 같은 `/login`이지만 GET과 POST는 완전히 다른 코드가 처리**
헷갈리기 쉬운 지점: `GET /login`과 `POST /login`은 URL만 같을 뿐 처리 주체가 다르다.
- `GET /login`: 우리가 만든 `LoginController.loginForm()`이 처리 → `user/login.html` 렌더링. 진입점은 `navbar.html`의 "로그인" 링크(`th:href="@{/login}"`)나 `signup.html`의 "로그인" 링크.
- `POST /login`: 컨트롤러에 `@PostMapping("/login")`이 아예 없음. `SecurityConfig`의 `formLogin(...)`이 자동으로 등록하는 `UsernamePasswordAuthenticationFilter`가 이 요청을 가로채서 처리 — 컨트롤러까지 도달하지 않음.

전체 흐름:
```
navbar "로그인" 클릭 → GET /login → LoginController → login.html 렌더링
login.html 폼 제출 → POST /login → [Spring Security 필터가 가로챔]
  → CustomUserDetailsService.loadUserByUsername(email)
  → PasswordEncoder.matches(입력값, DB값) + 계정 상태 체크
  → 성공: SecurityContext에 인증 저장 + "/"로 리다이렉트 (defaultSuccessUrl)
  → 실패: /login?error로 리다이렉트 (기본값)
```

**`loginPage`와 `loginProcessingUrl`의 관계 (기본값 상속, 우연 아님)**
`formLogin()`에는 별개의 두 설정이 있다.
- `loginPage(url)`: 로그인 화면 GET 경로 + 미인증 시 리다이렉트할 경로
- `loginProcessingUrl(url)`: 로그인 폼 제출(POST)을 받을 경로

이 프로젝트는 `loginProcessingUrl`을 따로 설정하지 않았는데, 이때 `/login`으로 정해지는 건 우연이 아니라 Spring Security 내부 로직(`AbstractAuthenticationFilterConfigurer.updateAuthenticationDefaults()`) 때문이다. `loginProcessingUrl`이 `null`이면 `loginPage` 값을 그대로 물려받도록 되어 있음 (Spring Security 공식 소스로 확인: `if (this.loginProcessingUrl == null) { loginProcessingUrl(this.loginPage); }`).
→ 만약 로그인 폼 action을 다른 경로로 바꾸고 싶다면, `loginProcessingUrl`을 명시적으로 지정해야 폼과 매칭됨.

---

## (다음 항목은 앞으로 질문할 때마다 追加)
