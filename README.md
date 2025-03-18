# OAuth API Demo

이 프로젝트는 Spring Boot와 MongoDB를 사용하여 OAuth 인증 서버를 구현한 예제입니다.

## MongoDB 설정 방법

1. **MongoDB 설치**:
   - [MongoDB Community Server](https://www.mongodb.com/try/download/community)에서 설치 파일을 다운로드합니다.
   - 설치 과정에서 기본 설정을 유지하고 설치를 완료합니다.

2. **MongoDB 실행**:
   - Windows의 경우, MongoDB는 서비스로 설치되며 자동으로 시작됩니다.
   - 수동으로 시작하려면 명령 프롬프트(관리자 권한)에서 다음 명령어를 실행합니다:
     ```bash
     net start MongoDB
     ```

3. **MongoDB 접속**:
   - MongoDB Shell을 사용하여 데이터베이스에 접속할 수 있습니다.
   - 명령 프롬프트에서 `mongosh`를 입력하여 MongoDB Shell을 실행합니다.

4. **데이터베이스 및 컬렉션 생성**:
   - `oauth_demo` 데이터베이스를 사용합니다:
     ```javascript
     use oauth_demo
     ```
   - `clients` 컬렉션은 애플리케이션 실행 시 자동으로 생성됩니다.

## 프로젝트 실행 방법

1. **MongoDB 실행**: MongoDB가 로컬에서 실행 중이어야 합니다. 기본 포트는 `27017`입니다.

2. **프로젝트 빌드 및 실행**:
   ```bash
   ./gradlew bootRun
   ```

3. **애플리케이션이 실행되면**: 기본적으로 `http://localhost:8080`에서 애플리케이션이 실행됩니다.

## API 테스트 방법

### 클라이언트 등록

클라이언트를 등록하여 `clientId`와 `clientSecret`을 발급받습니다.

```bash
curl -X POST http://localhost:8080/api/register
```

**응답 예시**:
```json
{
  "clientId": "c51774d6-e53b-4db3-8ad2-50be62882223",
  "clientSecret": "617c6845-04d2-45fe-a117-2980004ee0f8"
}
```

### 토큰 발급

발급받은 `clientId`와 `clientSecret`을 사용하여 토큰을 발급받습니다.

```bash
curl -X POST "http://localhost:8080/api/token?clientId=YOUR_CLIENT_ID&clientSecret=YOUR_CLIENT_SECRET"
```

### 보호된 리소스 접근

발급받은 토큰을 사용하여 보호된 리소스에 접근합니다.

```bash
curl -X GET http://localhost:8080/api/test -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 토큰 검증 소스

토큰 검증은 Spring Security의 OAuth2 리소스 서버 기능을 통해 자동으로 이루어집니다. `SecurityConfig` 클래스에서 설정된 내용은 다음과 같습니다:

```java
// SecurityConfig.java
.oauth2ResourceServer(oauth2 -> oauth2
    .jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter()))
);
```

이 설정은 모든 요청에 대해 JWT 토큰을 검증하며, 유효하지 않거나 만료된 토큰에 대해서는 자동으로 요청을 차단합니다.

## 토큰 만료 시간 설정

토큰의 만료 시간은 `TokenService` 클래스에서 설정됩니다. 기본적으로 토큰은 1시간 동안 유효합니다.

```java
// TokenService.java
public String generateToken(String clientId) {
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plus(1, ChronoUnit.HOURS)) // 토큰 만료 시간 설정
            .subject(clientId)
            .claim("scope", "api:access")
            .build();
    
    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
}
```

이 설정을 통해 토큰의 유효 기간을 조정할 수 있습니다. 