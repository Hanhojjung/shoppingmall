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

## (다음 항목은 앞으로 질문할 때마다 追加)
