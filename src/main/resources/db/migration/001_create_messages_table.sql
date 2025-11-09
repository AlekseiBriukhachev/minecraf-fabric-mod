CREATE TABLE IF NOT EXISTS messages (
    id SERIAL PRIMARY KEY,
    uuid UUID NOT NULL,
    text VARCHAR(256) NOT NULL,
);

CREATE INDEX IF NOT EXISTS idx_messages_uuid ON messages(uuid);

CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages(created_at);