--Order Service DB initilaisation

--User with bcrypt hashed passwords (password123)
INSERT INTO users (username, email, password_hash, role, created_at, is_active)
VALUES
    ('trader1', 'trader1@example.com', '$2a$10$yqaB3PhYhNJzNa7PiJVVfOfS.nGE8j8vLfMWtN7k9BF7BEchBmRTS', 'TRADER', CURRENT_TIMESTAMP, true),
    ('admin1', 'admin1@example.com', '$2a$10$yqaB3PhYhNJzNa7PiJVVfOfS.nGE8j8vLfMWtN7k9BF7BEchBmRTS', 'ADMIN', CURRENT_TIMESTAMP, true),
    ('trader2', 'trader2@example.com', '$2a$10$yqaB3PhYhNJzNa7PiJVVfOfS.nGE8j8vLfMWtN7k9BF7BEchBmRTS', 'TRADER', CURRENT_TIMESTAMP, true)
ON CONFLICT (username) DO NOTHING;

--orders for testing
--INSERT INTO orders (user_id, symbol, side, quantity, order_type, limit_price, status, workflow_id, created_at, updated_at)
--VALUES
--    ((SELECT id FROM users WHERE username = 'trader1'), 'AAPL', 'BUY', 100, 'MARKET', NULL, 'PENDING', 'order-workflow-sample-1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
--    ((SELECT id FROM users WHERE username = 'trader1'), 'GOOGL', 'BUY', 10, 'LIMIT', 2750.00, 'PENDING', 'order-workflow-sample-2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
--    ((SELECT id FROM users WHERE username = 'trader2'), 'MSFT', 'SELL', 50, 'MARKET', NULL, 'FILLED', 'order-workflow-sample-3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
--    ((SELECT id FROM users WHERE username = 'trader2'), 'TSLA', 'BUY', 25, 'LIMIT', 240.00, 'REJECTED', 'order-workflow-sample-4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
--ON CONFLICT DO NOTHING;