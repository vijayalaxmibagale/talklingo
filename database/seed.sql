INSERT INTO users (name, email, password, role)
VALUES (
    'Demo User',
    'demo@talklingo.live',
    '$2a$10$Q5dA2E4aZP7sI2i0RbL9QeWlY4d6rXzF6SgkHPXacF1lQ0X2kC7d2',
    'USER'
);

INSERT INTO conversation_sessions (title, source_language, target_language, status, owner_email, created_at)
VALUES (
    'Customer support bridge',
    'en',
    'es',
    'ACTIVE',
    'demo@talklingo.live',
    CURRENT_TIMESTAMP
);
