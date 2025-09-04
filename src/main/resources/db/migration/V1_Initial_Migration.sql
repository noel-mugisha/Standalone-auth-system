-- V1__Create_users_table.sql

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255),
                       role VARCHAR(50) NOT NULL DEFAULT 'USER',
                       email_verified BOOLEAN NOT NULL DEFAULT FALSE,
                       linkedin_id VARCHAR(255) UNIQUE,
                       otp VARCHAR(6),
                       otp_generated_time TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);