```mermaid
erDiagram
    EVENTS {
      INTEGER id PK "autoincrement"
      TEXT event_id "unique"
      TEXT type
      TEXT account_key
      TEXT payload
      TEXT status
      INTEGER attempts
      TEXT error_message
      DATETIME created_at
      DATETIME processing_started_at
      DATETIME processed_at
    }

    ACCOUNTS {
      INTEGER id PK "autoincrement"
      TEXT account_key "unique"
      TEXT parent_account_key "nullable" "references parent account (hierarchy)"
      TEXT email
      BOOLEAN email_forwarding
      TEXT status
      DATETIME created_at
      DATETIME updated_at
    }

    EVENTS }o--|| ACCOUNTS : "references account_key"

    %% Notes:
    %% - {APPLE}_ACCOUNT_DELETED represents a provider-level (top-level) account deletion.
    %%   When such event is processed, worker should mark the top-level account as deleted
    %%   and consider cascading or marking child accounts accordingly (business rule).
    %% - Indexes / constraints notes:
    %%   - EVENTS.event_id : UNIQUE
    %%   - EVENTS.account_key -> ACCOUNTS.account_key (logical FK)
    %%   - EVENTS.status ∈ {RECEIVED, PROCESSING, DONE, FAILED}
```
