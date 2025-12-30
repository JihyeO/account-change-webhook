# 실행 방법

요약: 로컬에서 애플리케이션을 실행하려면 JDK 17과 Gradle wrapper(내장)를 사용합니다. 데이터베이스 초기화는 `src/main/resources/schema.sql`이 애플리케이션 시작 시 자동으로 실행되도록 설정되어 있습니다.

Prerequisites:

- JDK 17
- Git, Gradle wrapper (프로젝트에 포함됨)

Quick start (Windows PowerShell):

Note: 이 저장소는 `src/main/resources/application.yml`에서 프로젝트 루트의 `application-local.yml`을 자동으로 임포트하도록 설정되어 있습니다. 따라서 로컬 개발 시에는 루트에 `application-local.yml`을 두면 별도 프로파일이나 환경변수를 설정하지 않아도 로컬 설정(`webhook.secret`)이 적용됩니다.

Option A — 로컬 전용 파일 사용(권장 개발 방식): 루트에 `application-local.yml`을 생성하고 `webhook.secret: test`처럼 설정합니다. 이후 평소처럼 실행하면 로컬 파일이 자동으로 적용됩니다.

Option B — 간단히 환경변수 사용:

```powershell
$Env:WEBHOOK_SECRET = 'your_secret_here'
mkdir -Force db
./gradlew.bat bootRun
```

```powershell
mkdir -Force db
./gradlew.bat bootRun
```

Quick start (macOS / Linux):

```bash
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

### 로컬 전용 설정 파일 (권장 개발 방식)

개발 시 시크릿을 코드에 직접 두지 않으려면 `application-local.yml` 파일을 프로젝트 루트에 만들어 사용하세요. 이 파일은 이미 `.gitignore`에 추가되어 있으므로 깃에 커밋되지 않습니다.

예시(`application-local.yml`):

```yaml
webhook:
  secret: test
```

설정 방법(간단):

- 이 저장소는 `application.yml`에서 루트의 `application-local.yml`을 자동으로 임포트하도록 설정되어 있습니다. 따라서 별도 프로파일을 지정하지 않아도 프로젝트 루트에 `application-local.yml`을 두면 애플리케이션이 이를 읽어옵니다.

실행 예시 (Windows PowerShell):

```powershell
# application-local.yml에 webhook.secret: test 와 같이 설정해 두었다고 가정
mkdir -Force db
./gradlew.bat bootRun
```

원한다면 `SPRING_PROFILES_ACTIVE=local`로 기존 방식처럼 실행할 수도 있습니다. (자동 임포트는 로컬 개발 편의성을 위한 기능입니다.)

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
