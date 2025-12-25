--Order Service DB initilaisation

--User with bcrypt hashed passwords (password123)
INSERT INTO users (username, email, password_hash, role, created_at, is_active)
VALUES
    ('trader1', 'trader1@example.com', '$2a$10$yqaB3PhYhNJzNa7PiJVVfOfS.nGE8j8vLfMWtN7k9BF7BEchBmRTS', 'TRADER', CURRENT_TIMESTAMP, true),
    ('admin1', 'admin1@example.com', '$2a$10$yqaB3PhYhNJzNa7PiJVVfOfS.nGE8j8vLfMWtN7k9BF7BEchBmRTS', 'ADMIN', CURRENT_TIMESTAMP, true),
    ('trader2', 'trader2@example.com', '$2a$10$yqaB3PhYhNJzNa7PiJVVfOfS.nGE8j8vLfMWtN7k9BF7BEchBmRTS', 'TRADER', CURRENT_TIMESTAMP, true)
ON CONFLICT (username) DO NOTHING;

-- Initialize wallets for users
INSERT INTO user_wallet (user_id, balance, currency, updated_at)
VALUES
    ((SELECT id FROM users WHERE username = 'trader1'), 50000.00, 'USD', CURRENT_TIMESTAMP),
    ((SELECT id FROM users WHERE username = 'admin1'), 100000.00, 'USD', CURRENT_TIMESTAMP),
    ((SELECT id FROM users WHERE username = 'trader2'), 25000.00, 'USD', CURRENT_TIMESTAMP)
ON CONFLICT (user_id) DO NOTHING;
