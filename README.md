# account-change-webhook

## 기능적 요구사항 분석

### 1. Webhok 수신 및 검증

- 서버는 환경변수 WEBHOOK_SECRET 기반으로 HMAC을 검증한다.
- 서명 검증 실패 시 401 또는 403을 반환한다.

### 2. Idempotency

- X-Event-Id 기준으로 중복 이벤트를 감지한다.
- 동일 eventId가 재전송되면 아래 case에 따라 응답한다.
  - 이미 처리 완료 시: 200과 함께 "이미 처리됨" 응답
  - 처리 중인 경우: 200과 함께 "처리 중" 응답

### 3. 이벤트 저장 및 처리 상태 관리

- 이벤트는 저장되어야 한다.
- 최소 상태는 RECEIVED -> PROCESSING -> DONE 또는 FAILED를 포함한다.
- Webhook 요청은 빠르게 반환해야 한다.

### 4. 처리해야 하는 이벤트

- EMAIL_FORWARDING_CHANGED: 계정의 이메일을 갱신
- ACCOUNT_DELETED: 계정 상태를 DELETED로 변경
- {APPLE}\_ACCOUNT_DELETED: 계정 상태를 {APPLE}\_DELETED로 변경 (사용자가 {APPLE}계정을 영구 삭제한 경우 같은 “상위 계정 레벨의 사건”임)

### 5. 필수 API

1. Webhook 수신
   POST /webhooks/account-changes
2. 계정 상태 조회
   GET /accounts/{accountKey}
3. 이벤트 처리 결과 조회
   GET /inbox/events/{eventId}
4. (선택) 처리 트리거
   POST /inbox/process

   비동기 처리 방식을 선택한 경우, 수동 트리거/테스트 편의를 위해 제공 가능, 스케줄러로 대체해도 무방

## 비기능적 요구사항 분석

### 1. 동시성

- 동일한 이벤트가 중복되어 처리되지 않도록 해야한다. (트랜잭션 / 락 사용)

### 2. 성능

- Webhook 요청을 빠르게 반환하기 위해 처리해야 한다. (메시지 큐 등)

### 3. 유지보수성

- 모든 기능에 대해 최소 1개 이상의 테스트케이스를 작성해야 한다.
