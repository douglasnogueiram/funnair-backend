-- ========================================
-- Flight Booking Database - Schema & Demo Data
-- ========================================
-- Database: flight_booking
-- Execute this script manually in PostgreSQL
-- ========================================

-- Drop existing tables if they exist
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS agent_prompts CASCADE;

-- ========================================
-- CREATE TABLES
-- ========================================

-- Agent prompts table (versioned)
CREATE TABLE IF NOT EXISTS agent_prompts (
    id          BIGSERIAL PRIMARY KEY,
    content     TEXT        NOT NULL,
    description VARCHAR(500),
    is_active   BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP   NOT NULL
);

-- Customers table
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL
);

-- Bookings table
CREATE TABLE bookings (
    booking_number VARCHAR(10) PRIMARY KEY,
    date DATE NOT NULL,
    booking_to DATE,
    customer_id BIGINT NOT NULL,
    from_airport VARCHAR(3),
    destination VARCHAR(3),
    booking_status VARCHAR(20) NOT NULL,
    seat_number VARCHAR(10),
    booking_class VARCHAR(20) NOT NULL,
    CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Payment Transactions table
CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    booking_id VARCHAR(10) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    payment_id VARCHAR(100),
    card_id VARCHAR(50),
    card_last_four VARCHAR(4),
    amount DECIMAL(10, 2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    error_message VARCHAR(500),
    CONSTRAINT fk_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_number) ON DELETE CASCADE
);

-- ========================================
-- INSERT DEMO DATA
-- ========================================

-- Insert customers
INSERT INTO customers (id, first_name, last_name) VALUES
(1, 'John', 'Doe'),
(2, 'Jane', 'Smith'),
(3, 'Michael', 'Johnson'),
(4, 'Sarah', 'Williams'),
(5, 'Robert', 'Taylor');

-- Reset sequence for customers
SELECT setval('customers_id_seq', 5, true);

-- Insert bookings
INSERT INTO bookings (booking_number, date, booking_to, customer_id, from_airport, destination, booking_status, seat_number, booking_class) VALUES
('101', CURRENT_DATE, NULL, 1, 'LAX', 'JFK', 'CONFIRMED', '12A', 'ECONOMY'),
('102', CURRENT_DATE + INTERVAL '2 days', NULL, 2, 'SFO', 'LHR', 'CONFIRMED', '5B', 'BUSINESS'),
('103', CURRENT_DATE + INTERVAL '4 days', NULL, 3, 'JFK', 'CDG', 'CONFIRMED', '18C', 'ECONOMY'),
('104', CURRENT_DATE + INTERVAL '6 days', NULL, 4, 'LHR', 'FRA', 'CONFIRMED', '3A', 'PREMIUM_ECONOMY'),
('105', CURRENT_DATE + INTERVAL '8 days', NULL, 5, 'CDG', 'MAD', 'CONFIRMED', '15D', 'BUSINESS');

-- ========================================
-- VERIFY DATA
-- ========================================

-- Count records
SELECT 'Customers' as table_name, COUNT(*) as record_count FROM customers
UNION ALL
SELECT 'Bookings' as table_name, COUNT(*) as record_count FROM bookings;

-- Display all data
SELECT 
    b.booking_number,
    c.first_name,
    c.last_name,
    b.date,
    b.from_airport,
    b.destination,
    b.seat_number,
    b.booking_class,
    b.booking_status
FROM bookings b
JOIN customers c ON b.customer_id = c.id
ORDER BY b.booking_number;
