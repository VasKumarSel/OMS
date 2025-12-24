--Market data DB initialisation

--market_prices
CREATE TABLE IF NOT EXISTS market_prices (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL UNIQUE,
    bid_price DECIMAL(10,4) NOT NULL,
    ask_price DECIMAL (10,4)  NOT NULL,
    last_price DECIMAL (10,4)  NOT NULL,
    volume BIGINT DEFAULT 0,
    updated_at TIMESTAMP NOT NUll DEFAULT CURRENT_TIMESTAMP
);

-- Insert
INSERT INTO market_prices (symbol, bid_price, ask_price, last_price, volume)
VALUES
    ('AAPL', 150.25, 150.30, 150.28, 1000000),
    ('GOOGL', 2750.50, 2751.00, 2750.75, 500000),
    ('MSFT', 380.15, 380.20, 380.18, 800000),
    ('TSLA', 245.60, 245.70, 245.65, 1200000),
    ('AMZN', 155.80, 155.85, 155.82, 600000),
    ('META', 350.40, 350.45, 350.42, 400000),
    ('NFLX', 485.10, 485.15, 485.12, 300000),
    ('NVDA', 875.60, 875.70, 875.65, 900000)
ON CONFLICT (symbol) DO NOTHING;