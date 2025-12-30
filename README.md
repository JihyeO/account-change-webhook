# 실행 방법

요약: 로컬에서 애플리케이션을 실행하려면 JDK 17과 Gradle wrapper(내장)를 사용합니다. 데이터베이스 초기화는 `src/main/resources/schema.sql`이 애플리케이션 시작 시 자동으로 실행되도록 설정되어 있습니다.

Prerequisites:

- JDK 17
- Git, Gradle wrapper (프로젝트에 포함됨)

Quick start (Windows PowerShell):

```powershell
$Env:WEBHOOK_SECRET = 'your_secret_here'
mkdir -Force db
./gradlew.bat bootRun
```

Quick start (macOS / Linux):

```bash
export WEBHOOK_SECRET=your_secret_here
mkdir -p db
./gradlew bootRun
```

Build jar and run:

```bash
./gradlew bootJar
java -jar build/libs/*-0.0.1-SNAPSHOT.jar
```

Run tests:

```bash
./gradlew test
```

DB initialization:

- `schema.sql` (SQLite) 는 `spring.sql.init.mode=always` 설정으로 애플리케이션 시작 시 `db/webhook.db`에 테이블을 생성합니다. 수동 초기화가 필요하면 `sqlite3 db/webhook.db < src/main/resources/schema.sql`로 적용하세요. <br><br>

# API 명세 요약

## 1) Webhook 수신

- 경로: `POST /webhooks/account-changes`
- 설명: 외부에서 계정 변경 이벤트를 수신하여 HMAC 검증 후 이벤트로 저장하거나 상태에 따라 응답합니다.
- 필수 헤더:
  - `X-Signature`: HMAC-SHA256 hex (서명 검증에 사용)
  - `X-Event-Id`: 이벤트 고유 ID (중복 방지 및 조회 키)
- 요청 바디: raw JSON (서비스는 PoC로 `type`, `accountKey`, `provider`를 간단 추출함)
- 응답: 200 OK (본문 문자열: `accepted`, `이미 처리됨`, `처리 중` 등), 400/401 에러 가능

## 2) 계정 조회

- 경로: `GET /accounts/{accountKey}`
- 설명: 계정 정보를 `accountKey`로 조회
- 응답: 200 OK (Account 객체 JSON) 또는 404 Not Found

## 3) 이벤트 조회 (Inbox)

- 경로: `GET /inbox/events/{eventId}`
- 설명: 저장된 이벤트를 `eventId`로 조회
- 응답: 200 OK (Event 객체 JSON) 또는 404 Not Found

## 응답 모델 요약

- `Event`:
  - `id`: number | null
  - `eventId`: string
  - `type`: string
  - `accountKey`: string | null
  - `provider`: string | null
  - `payload`: string | null
  - `status`: string (예: RECEIVED, QUEUED, PROCESSING, DONE)
  - `attempts`: integer
  - `errorMessage`: string | null
- `Account`:
  - `id`: number | null
  - `accountKey`: string
  - `parentAccountKey`: string | null
  - `email`: string | null
  - `emailForwarding`: boolean
  - `status`: string

## 보안/운영 메모

- 서명 검증에 필요한 시크릿: 환경변수 `WEBHOOK_SECRET`
- 현재 페이로드 파싱은 정규식 기반 PoC임 — 프로덕션에서는 Jackson 등 JSON 파서 사용

### `WEBHOOK_SECRET` 설정 (환경변수)

- 필수: 애플리케이션 시작 전에 `WEBHOOK_SECRET` 환경변수가 설정되어야 합니다.
- 예시 (로컬 macOS/Linux):

  ```bash
  export WEBHOOK_SECRET=your_secret_here
  ./gradlew bootRun
  ```

- 예시 (Windows PowerShell):
  ```powershell
  $Env:WEBHOOK_SECRET = 'your_secret_here'
  ./gradlew bootRun
  ```

## Swagger UI로 API 문서 확인 및 테스트

애플리케이션을 실행하면 자동으로 OpenAPI 문서가 생성되고 Swagger UI에서 직접 호출해 볼 수 있습니다.

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

실행 (Windows PowerShell):

```powershell
$Env:WEBHOOK_SECRET = 'your_secret_here'
./gradlew.bat bootRun
```

테스트용 엔드포인트 예시 (Swagger UI나 curl에서 사용):

- 엔드포인트: `POST /webhooks/account-changes/test`
- 필요한 헤더: `X-Signature`, `X-Event-Id`

curl 예시:

```bash
curl -X POST 'http://localhost:8080/webhooks/account-changes/test' \
  -H 'Content-Type: application/json' \
  -H 'X-Signature: <signature>' \
  -H 'X-Event-Id: evt-123' \
  -d '{"type":"ACCOUNT_UPDATED","accountKey":"abc123","provider":"bank","data":{"email":"user@example.com"}}'
```

설명:

- Swagger UI에서 바디를 직접 입력하고 `X-Signature`, `X-Event-Id` 헤더를 추가하면 즉시 호출해볼 수 있습니다.
- 실제 프로덕션 수신점은 `POST /webhooks/account-changes`입니다. Swagger로 바로 바디와 헤더를 편하게 테스트하려면 위의 `/test` 엔드포인트를 사용하세요.
