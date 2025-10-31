-- Payment System Migration Script
-- This script creates the payments table and related indexes

-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    id SERIAL PRIMARY KEY,
    rental_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    paid_at TIMESTAMP,
    due_date TIMESTAMP,
    transaction_id VARCHAR(100),
    payment_reference VARCHAR(500),
    notes TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_payments_rental_id FOREIGN KEY (rental_id) REFERENCES rentals(id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_customer_id FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    
    -- Check constraints for enums
    CONSTRAINT chk_payment_method CHECK (method IN ('CREDIT_CARD', 'BANK_TRANSFER', 'CASH', 'CHECK', 'DIGITAL_WALLET', 'ONLINE_BANKING')),
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED', 'PARTIAL'))
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_payments_rental_id ON payments(rental_id);
CREATE INDEX IF NOT EXISTS idx_payments_customer_id ON payments(customer_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_due_date ON payments(due_date);
CREATE INDEX IF NOT EXISTS idx_payments_paid_at ON payments(paid_at);
CREATE INDEX IF NOT EXISTS idx_payments_transaction_id ON payments(transaction_id);

-- Add comments for documentation
COMMENT ON TABLE payments IS 'Payment records for rental transactions';
COMMENT ON COLUMN payments.rental_id IS 'Reference to the rental this payment belongs to';
COMMENT ON COLUMN payments.customer_id IS 'Reference to the customer making the payment';
COMMENT ON COLUMN payments.amount IS 'Payment amount in decimal format';
COMMENT ON COLUMN payments.method IS 'Payment method used (CREDIT_CARD, BANK_TRANSFER, etc.)';
COMMENT ON COLUMN payments.status IS 'Payment status (PENDING, COMPLETED, FAILED, etc.)';
COMMENT ON COLUMN payments.paid_at IS 'Timestamp when payment was completed';
COMMENT ON COLUMN payments.due_date IS 'Timestamp when payment is due';
COMMENT ON COLUMN payments.transaction_id IS 'External transaction ID from payment gateway';
COMMENT ON COLUMN payments.payment_reference IS 'Additional payment reference information';
COMMENT ON COLUMN payments.notes IS 'Additional notes about the payment';

-- Sample data for testing (optional)
INSERT INTO payments (rental_id, customer_id, amount, method, status, paid_at, due_date, notes) VALUES
(1, 1, 500.00, 'CREDIT_CARD', 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Initial payment for rental'),
(2, 2, 750.00, 'BANK_TRANSFER', 'PENDING', NULL, CURRENT_TIMESTAMP + INTERVAL '7 days', 'Pending bank transfer'),
(3, 3, 300.00, 'CASH', 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Cash payment at pickup')
ON CONFLICT DO NOTHING;


