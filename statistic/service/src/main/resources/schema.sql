DROP TABLE IF EXISTS endpointhit CASCADE;

CREATE TABLE IF NOT EXISTS endpointhit (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app VARCHAR(50) NOT NULL,
    uri VARCHAR(50) NOT NULL,
    ip VARCHAR(50) NOT NULL,
    date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL
);