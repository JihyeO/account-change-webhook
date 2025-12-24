-- SQLite schema for account-change-webhook (copied from schema/)
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS accounts (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  account_key TEXT NOT NULL UNIQUE,
  parent_account_key TEXT,
  email TEXT,
  email_forwarding INTEGER DEFAULT 0,
  status TEXT DEFAULT 'ACTIVE',
  created_at DATETIME DEFAULT (datetime('now')),
  updated_at DATETIME DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS events (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  event_id TEXT NOT NULL UNIQUE,
  type TEXT NOT NULL,
  provider TEXT,
  account_key TEXT,
  payload TEXT,
  status TEXT NOT NULL DEFAULT 'RECEIVED',
  attempts INTEGER NOT NULL DEFAULT 0,
  error_message TEXT,
  created_at DATETIME DEFAULT (datetime('now')),
  processing_started_at DATETIME,
  processed_at DATETIME,
  FOREIGN KEY (account_key) REFERENCES accounts(account_key) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_events_status ON events(status);
CREATE INDEX IF NOT EXISTS idx_events_created_at ON events(created_at);

CREATE TABLE IF NOT EXISTS work_queue (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  event_id TEXT NOT NULL,
  enqueued_at DATETIME DEFAULT (datetime('now')),
  processed INTEGER DEFAULT 0,
  UNIQUE(event_id)
);
