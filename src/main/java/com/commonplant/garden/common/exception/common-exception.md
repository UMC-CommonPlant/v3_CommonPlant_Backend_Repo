# 공통 예외 처리 가이드 (Error Code Usage)

> 참고: [Springboot 공통 예외처리를 위한 로직 - bear's blog](https://okbear3.tistory.com/84)

---

## 1. 설계 목표

| 목표 | 설명 |
|---|---|
| **일관된 에러 응답** | 모든 예외를 표준화된 JSON 형식으로 클라이언트에게 반환 |
| **관심사 분리 (SoC)** | Service/Controller는 비즈니스 로직에만 집중, 예외 처리는 `GlobalExceptionHandler`에 위임 |
| **도메인별 독립 관리** | 각 도메인이 자신의 에러 코드 파일만 수정 → 협업 시 충돌 최소화 |
| **확장성** | `ErrorCode` 인터페이스를 구현하기만 하면 어느 Enum이든 동일하게 처리 |

---

## 2. 전체 구조

```
com.example.sns/
│
├── common/exception/
│   ├── ErrorCode.java              # 인터페이스 (공통 계약)
│   ├── CommonErrorCode.java        # 공통 에러 코드 Enum (C001~)
│   ├── BusinessException.java      # 커스텀 예외
│   ├── ErrorResponse.java          # 클라이언트 응답 형식
│   └── GlobalExceptionHandler.java # 전역 예외 핸들러
└── domain/
    ├── auth/exception/
    │   └── AuthErrorCode.java      # 인증 에러 코드 Enum (A001~)
    ├── user/exception/
    │   └── UserErrorCode.java      # 사용자 에러 코드 Enum (U001~)
    ├── post/exception/
    │   └── PostErrorCode.java      # 게시글 에러 코드 Enum (P001~)
    └── comment/exception/
        └── CommentErrorCode.java   # 댓글 에러 코드 Enum (CM001~)
```

### 에러 코드 파일 소유권
각 담당자는 **자신의 도메인 파일만 수정**합니다. 충돌이 나는 구간이 완전히 분리됩니다.

| 파일 | 담당 |
|---|---|
| `CommonErrorCode.java` | 공통 담당자 |
| `AuthErrorCode.java` | 인증 담당자 |
| `UserErrorCode.java` | 유저 도메인 담당자 |
| `PostErrorCode.java` | 게시글 도메인 담당자 |
| `CommentErrorCode.java` | 댓글 도메인 담당자 |

---
## 3. ErrorCode 인터페이스
모든 도메인 에러 코드 Enum이 구현하는 **공통 계약**입니다.
`BusinessException`은 이 인터페이스 타입을 받으므로 어느 도메인 Enum을 넣어도 동일하게 동작합니다.
```java
// common/exception/ErrorCode.java
public interface ErrorCode {
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
```

---
## 4. 도메인별 ErrorCode Enum
각 도메인 패키지 안에 독립적인 Enum을 두고 `ErrorCode` 인터페이스를 구현합니다.

```java
// common/exception/CommonErrorCode.java
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,             "C001", "잘못된 입력 값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,       "C002", "허용되지 않는 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND,                  "C003", "엔티티를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 내부 오류입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST,              "C005", "잘못된 타입입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN,                     "C006", "접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
```

### ex) UserErrorCode
```java
// domain/user/exception/UserErrorCode.java
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND,           "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT,           "U002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT,        "U003", "이미 사용 중인 닉네임입니다."),
    SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST,"U004", "자기 자신을 팔로우할 수 없습니다."),
    ALREADY_FOLLOWING(HttpStatus.CONFLICT,         "U005", "이미 팔로우한 사용자입니다."),
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND,         "U006", "팔로우 관계를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
```
---

## 5. 사용 방법 (Service / Controller)

각 Service는 **자신의 도메인 ErrorCode만** import합니다.

```java
// PostService.java
import com.example.sns.domain.post.exception.PostErrorCode;

public Post findPost(Long postId) {
    return postRepository.findById(postId)
            .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));
}

public void deletePost(Long postId, Long userId) {
    Post post = findPost(postId);
    if (!post.isAuthor(userId)) {
        throw new BusinessException(PostErrorCode.POST_AUTHOR_MISMATCH);
    }
    postRepository.delete(post);
}
```

```java
// UserService.java
import com.example.sns.domain.user.exception.UserErrorCode;

public void followUser(Long followerId, Long followeeId) {
    if (followerId.equals(followeeId)) {
        throw new BusinessException(UserErrorCode.SELF_FOLLOW_NOT_ALLOWED);
    }
    // ...
}

public void validateNickname(String nickname) {
    if (userRepository.existsByNickname(nickname)) {
        throw new BusinessException(
            UserErrorCode.DUPLICATE_NICKNAME,
            String.format("'%s'은(는) 이미 사용 중인 닉네임입니다.", nickname)
        );
    }
}
```

### ❌ 피해야 할 패턴

```java
// ❌ 다른 도메인의 에러 코드를 가져다 쓰기
// PostService에서 UserErrorCode를 사용하지 마세요
throw new BusinessException(UserErrorCode.USER_NOT_FOUND);

// ❌ 에러 코드 없이 RuntimeException 직접 사용
throw new RuntimeException("게시글을 찾을 수 없습니다.");
```

---

## 6. 에러 코드 명명 규칙

```
{도메인 접두사}_{동사/형용사}_{명사}

예시:
  USER_NOT_FOUND         → 사용자 없음
  DUPLICATE_NICKNAME     → 닉네임 중복
  POST_AUTHOR_MISMATCH   → 작성자 불일치
  INVALID_TOKEN          → 유효하지 않은 토큰
  IMAGE_SIZE_EXCEEDED    → 이미지 크기 초과
```

### 네이밍 패턴

| 상황 | 패턴 | 예시 |
|---|---|---|
| 리소스를 찾을 수 없음 | `{DOMAIN}_NOT_FOUND` | `USER_NOT_FOUND` |
| 중복 | `DUPLICATE_{FIELD}` | `DUPLICATE_NICKNAME` |
| 권한 없음 | `{DOMAIN}_AUTHOR_MISMATCH` | `POST_AUTHOR_MISMATCH` |
| 이미 존재 | `ALREADY_{ACTION}` | `ALREADY_LIKED` |
| 유효하지 않음 | `INVALID_{FIELD}` | `INVALID_TOKEN` |
| 초과 | `{FIELD}_EXCEEDED` | `IMAGE_SIZE_EXCEEDED` |

---

> `GlobalExceptionHandler`와 `BusinessException`은 **수정이 필요 없습니다.**  
> `ErrorCode` 인터페이스를 구현한 Enum이라면 자동으로 처리됩니다.