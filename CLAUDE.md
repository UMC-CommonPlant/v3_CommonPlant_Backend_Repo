# CLAUDE.md

This file provides guidance to Claude Code when working with this repository.
Read this entire file before making any changes to the codebase.

## Project Overview

**CommonPlant (커먼플랜트)** — 반려식물 관리 서비스.
사용자가 장소(Place), 식물(Plant), 메모(Memo), 캘린더(Calendar)를 친구와 함께 관리한다.

- **Language:** Java 17
- **Framework:** Spring Boot 3.x
- **Build Tool:** Gradle
- **Database:** H2 (local dev) / MySQL (production)
- **Base API path:** `/api/v1`

---

## Commands

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 전체 테스트
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "com.commonplant.garden.ClassName"

# H2 콘솔 (dev only)
# URL: http://localhost:8080/api/v1/h2-console
# JDBC URL: jdbc:h2:mem:commondb / username: sa / password: (없음)
```

---

## Package Structure

루트 패키지: `com.commonplant.garden`

```
com.commonplant.garden/
├── common/
│   ├── domain/BaseTime.java              # JPA Auditing (createdAt, updatedAt)
│   ├── dto/JsonResponse.java             # 공통 응답 래퍼
│   └── exception/
│       ├── ErrorCode.java                # 모든 도메인 ErrorCode enum이 구현하는 인터페이스
│       ├── CommonErrorCode.java          # 공통 에러코드 (prefix: C)
│       ├── BusinessException.java        # 프로젝트 전체에서 사용하는 단일 커스텀 예외
│       ├── ErrorResponse.java            # 에러 응답 JSON 형태
│       └── GlobalExceptionHandler.java
└── {domain}/                             # ex) user/
    ├── controller/
    ├── service/                          # Interface + Impl 분리
    ├── entity/
    ├── dto/
    │   ├── request/
    │   └── response/
    ├── enums/
    ├── exception/{Domain}ErrorCode.java
    └── repository/
```

> **현재 예외:** `UserRepository`는 `entity/` 패키지 안에 위치. 신규 도메인은 `repository/` 패키지를 별도 생성.

---

## Architecture Rules

### 1. 응답 형식 — JsonResponse

모든 API 응답은 `JsonResponse<T>`로 감싼다. 직접 Entity나 DTO를 반환하지 않는다.

```java
// Controller 반환 예시
return ResponseEntity.ok(JsonResponse.success(userService.getUser(userIdx)));

// JsonResponse 구조 (참고용)
{
  "isSuccess": true,
  "message": "요청에 성공하였습니다.",
  "result": { ... }
}
```

### 2. 예외 처리 — BusinessException + ErrorCode

**규칙:**
- 예외는 반드시 `BusinessException`만 사용한다. `RuntimeException` 직접 사용 금지.
- 각 도메인은 자신의 `{Domain}ErrorCode`만 사용한다. 다른 도메인 ErrorCode 참조 금지.
- ErrorCode prefix: `C` (Common), `U` (User), `P` (Plant), `PL` (Place) …

```java
// 1. 도메인별 ErrorCode 정의 (user/exception/UserErrorCode.java)
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,       "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT,        "U002", "이미 사용 중인 이메일입니다."),
    USER_UNAUTHORIZED(HttpStatus.FORBIDDEN,     "U003", "접근 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

// 2. Service에서 throw
User user = userRepository.findByUserIdxAndStatus(userIdx, UserStatus.ACTIVE)
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
```

**ErrorCode 네이밍 규칙:**

| 패턴 | 예시 |
|------|------|
| 리소스 없음 | `{DOMAIN}_NOT_FOUND` |
| 중복 | `DUPLICATE_{FIELD}` |
| 권한 없음 | `{DOMAIN}_UNAUTHORIZED` |
| 이미 존재 | `ALREADY_{ACTION}` |
| 유효하지 않음 | `INVALID_{FIELD}` |
| 한도 초과 | `{FIELD}_EXCEEDED` |

### 3. Service 레이어

- Interface + Impl 분리: `UserService` / `UserServiceImpl`
- 클래스 레벨에 `@Transactional(readOnly = true)` 적용
- 쓰기 메서드만 `@Transactional`로 오버라이드

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getUser(Long userIdx) { ... }

    @Override
    @Transactional
    public UserResponse updateUser(Long userIdx, UserUpdateRequest request) { ... }
}
```

### 4. Entity 규칙

- 모든 Entity는 `BaseTime` 상속 (createdAt, updatedAt 자동 관리)
- `@Builder`는 **클래스 레벨이 아닌 특정 생성자**에만 적용
- Soft delete: 행을 삭제하지 않고 `status` 필드를 `INACTIVE`로 변경
- 상태 변경은 Entity 내부 메서드를 통해서만 수행 (직접 setter 사용 금지)

```java
@Getter
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class User extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_idx")
    private Long userIdx;

    // ... 필드

    @Builder
    public User(String uuid, String name, String email, Provider provider,
                String providerId, String imgUrl) {
        this.uuid = uuid;
        this.name = name;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.imgUrl = imgUrl;
        this.status = UserStatus.ACTIVE;
    }

    // 도메인 메서드 (setter 대신 사용)
    public void updateProfile(String name, String introduction, String imgUrl) {
        if (name != null && !name.isBlank()) this.name = name;
        if (introduction != null) this.introduction = introduction;
        if (imgUrl != null) this.imgUrl = imgUrl;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
}
```

### 5. Controller 규칙

- `@RestController` + `@RequestMapping("/api/v1/{domain}s")`
- 수정은 `@PatchMapping` (전체 교체가 아닌 부분 수정)
- 삭제는 `@DeleteMapping` (내부적으로는 soft delete)
- 인증이 필요한 엔드포인트는 `@AuthenticationPrincipal` 또는 커스텀 어노테이션으로 사용자 정보 추출

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userIdx}")
    public ResponseEntity<JsonResponse<UserResponse>> getUser(@PathVariable Long userIdx) {
        return ResponseEntity.ok(JsonResponse.success(userService.getUser(userIdx)));
    }

    @PatchMapping("/{userIdx}")
    public ResponseEntity<JsonResponse<UserResponse>> updateUser(
            @PathVariable Long userIdx,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(JsonResponse.success(userService.updateUser(userIdx, request)));
    }
}
```

---

## What NOT To Do

다음 사항을 절대 하지 말 것:

- `RuntimeException`, `IllegalArgumentException` 직접 throw → `BusinessException` 사용
- 다른 도메인의 ErrorCode 참조 (ex. `UserService`에서 `PlantErrorCode` 사용)
- Entity를 Controller에서 직접 반환 → 반드시 ResponseDTO 변환 후 반환
- `@Transactional` 없이 쓰기(INSERT/UPDATE/DELETE) 작업 수행
- Entity에 public setter 추가 → 도메인 메서드로 상태 변경
- `common/exception/` 하위 파일 임의 수정 (GlobalExceptionHandler, BusinessException 등)
- 하드코딩된 문자열 에러 메시지 사용 → ErrorCode에 정의

---

## Git Conventions

**커밋 형식:** `[Type] Subject`

| Type | 설명 |
|------|------|
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `docs` | 문서 변경 |
| `style` | 포맷팅 (로직 변경 없음) |
| `refactor` | 리팩토링 |
| `test` | 테스트 추가/수정 |
| `chore` | 빌드/설정 변경 |

**브랜치 전략:**
```
main       ← 배포 브랜치
  └── develop       ← 통합 브랜치
        └── feature#이슈번호/기능명   ← 기능 개발
```

예시: `feature#10/user-crud-api`

---

## 신규 도메인 추가 체크리스트

새 도메인(ex. `Plant`) 추가 시 아래 순서로 작업한다:

```
[ ] 1. entity/{Domain}.java 생성 (BaseTime 상속, @Builder 생성자)
[ ] 2. enums/ 필요한 Enum 생성 (Status 등)
[ ] 3. exception/{Domain}ErrorCode.java 생성 (ErrorCode 구현)
[ ] 4. repository/{Domain}Repository.java 생성
[ ] 5. dto/request/{Domain}CreateRequest.java, {Domain}UpdateRequest.java 생성
[ ] 6. dto/response/{Domain}Response.java 생성
[ ] 7. service/{Domain}Service.java (interface) 생성
[ ] 8. service/{Domain}ServiceImpl.java 생성
[ ] 9. controller/{Domain}Controller.java 생성
[ ] 10. ./gradlew build 로 컴파일 오류 확인
[ ] 11. ./gradlew test 로 테스트 통과 확인
```