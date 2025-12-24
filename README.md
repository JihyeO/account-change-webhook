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
