CREATE TABLE IF NOT EXISTS rating (
    symbol VARCHAR(20),
    value VARCHAR(10),
    analyst VARCHAR(40),
    update_date_time datetime,
    PRIMARY KEY (symbol)
);