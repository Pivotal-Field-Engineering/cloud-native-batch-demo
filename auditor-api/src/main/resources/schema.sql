CREATE TABLE IF NOT EXISTS trade (
    trade_id BIGINT NOT NULL AUTO_INCREMENT,
    account_number VARCHAR(254),
    amount DECIMAL(19,2),
    rating VARCHAR(255),
    shares DECIMAL(19,2),
    symbol VARCHAR(255),
    trade_date_time datetime,
    update_date_time datetime,
    PRIMARY KEY (trade_id)
);