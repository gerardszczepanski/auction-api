CREATE TABLE auctions
(
    id VARCHAR(50) PRIMARY KEY NOT NULL,
    code VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL,
    minimal_price_amount NUMERIC(20,2) NOT NULL,
    minimal_price_currency VARCHAR(20) NOT NULL,
    start_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    end_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    creation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version SMALLINT DEFAULT 0
);

CREATE TABLE bets
(
    id VARCHAR(50) PRIMARY KEY NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    auction_id VARCHAR(50) NOT NULL,
    price_amount NUMERIC(20,2) NOT NULL,
    price_currency VARCHAR(20) NOT NULL,
    creation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (auction_id) REFERENCES auctions(id)
);


