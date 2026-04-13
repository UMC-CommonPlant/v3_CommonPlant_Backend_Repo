# 공통 예외 처리 가이드 (Error Code Usage)

> 참고: [Springboot 공통 예외처리를 위한 로직 - bear's blog](https://okbear3.tistory.com/84)

---

## 설계 목표

| 목표 | 설명 |
|---|---|
| **일관된 에러 응답** | 모든 예외를 표준화된 JSON 형식으로 클라이언트에게 반환 |
| **관심사 분리 (SoC)** | Service/Controller는 비즈니스 로직에만 집중, 예외 처리는 `GlobalExceptionHandler`에 위임 |
| **도메인별 독립 관리** | 각 도메인이 자신의 에러 코드 파일만 수정 → 협업 시 충돌 최소화 |
| **확장성** | `ErrorCode` 인터페이스를 구현하기만 하면 어느 Enum이든 동일하게 처리 |

---
## 구조

```
common/
├── exception/
│   ├── ErrorCode.java              # 인터페이스 (공통 계약)
│   ├── CommonErrorCode.java        # 공통 에러 코드 (C001~)
│   ├── BusinessException.java      # 커스텀 예외
│   ├── ErrorResponse.java          # 에러 응답 형식
│   └── GlobalExceptionHandler.java # 전역 예외 핸들러
└── filter/
    └── MdcLoggingFilter.java       # 요청 추적 필터

domain/
└── {도메인}/exception/
    └── {도메인}ErrorCode.java      # 도메인별 에러 코드
```

---

## 요청 흐름

```
요청 → MdcLoggingFilter (traceId 부여)
     → Controller / Service
     → 예외 발생 시 GlobalExceptionHandler
     → ErrorResponse 반환
```

```json
// 에러 응답 예시
{
  "traceId": "a1b2c3d4e5f6g7h8",
  "status": 404,
  "code": "U001",
  "message": "사용자를 찾을 수 없습니다.",
  "timestamp": "2024-04-13T10:30:00"
}
```

---

## 사용 방법

### 1. 도메인 ErrorCode 추가

각 도메인 담당자는 **자신의 파일만 수정**합니다.

```java
// domain/user/exception/UserErrorCode.java
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND,     "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT,     "U002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT,  "U003", "이미 사용 중인 닉네임입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
```

### 2. Service에서 예외 던지기

```java
// 기본 사용
throw new BusinessException(UserErrorCode.USER_NOT_FOUND);

// 동적 메시지가 필요한 경우
throw new BusinessException(UserErrorCode.DUPLICATE_NICKNAME,
        String.format("'%s'은(는) 이미 사용 중입니다.", nickname));
```

### 3. 하지 말아야 할 것

```java
// 다른 도메인 ErrorCode 사용 금지
throw new BusinessException(PostErrorCode.POST_NOT_FOUND); // UserService에서 사용 금지

// RuntimeException 직접 사용 금지
throw new RuntimeException("사용자를 찾을 수 없습니다.");
```

---

## 에러 코드 담당자

| 파일 | 접두사 | 담당 |
|---|---|---|
| `CommonErrorCode` | C | 공통 |
| `UserErrorCode` | U | 유저 |
| `PostErrorCode` | P | 게시글 |
| `CommentErrorCode` | CM | 댓글 |

---

## 에러 코드 명명 규칙

| 상황 | 패턴 | 예시 |
|---|---|---|
| 리소스 없음 | `{DOMAIN}_NOT_FOUND` | `USER_NOT_FOUND` |
| 중복 | `DUPLICATE_{FIELD}` | `DUPLICATE_EMAIL` |
| 권한 없음 | `{DOMAIN}_UNAUTHORIZED` | `POST_UNAUTHORIZED` |
| 이미 존재 | `ALREADY_{ACTION}` | `ALREADY_LIKED` |
| 유효하지 않음 | `INVALID_{FIELD}` | `INVALID_TOKEN` |
| 초과 | `{FIELD}_EXCEEDED` | `IMAGE_SIZE_EXCEEDED` |

---

## traceId

모든 요청에는 고유한 `traceId`가 부여됩니다.
클라이언트가 에러를 신고할 때 `traceId`를 제공하면 서버 로그에서 해당 요청을 즉시 추적할 수 있습니다.

```bash
grep "a1b2c3d4e5f6g7h8" application.log
```

---

> `GlobalExceptionHandler`, `BusinessException`, `MdcLoggingFilter`는 수정하지 않습니다.
> `ErrorCode` 인터페이스를 구현한 Enum이라면 자동으로 처리됩니다.