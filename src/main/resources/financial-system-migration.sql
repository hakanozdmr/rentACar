-- Financial System Complete Migration Script
-- This script creates all financial management tables

-- ==============================================
-- INVOICES TABLE
-- ==============================================
CREATE TABLE IF NOT EXISTS invoices (
    id SERIAL PRIMARY KEY,
    rental_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    issue_date TIMESTAMP NOT NULL,
    due_date TIMESTAMP NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal > 0),
    tax_rate DECIMAL(5,4),
    tax_amount DECIMAL(12,2),
    total_amount DECIMAL(12,2) NOT NULL CHECK (total_amount > 0),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    paid_at TIMESTAMP,
    payment_terms VARCHAR(500),
    notes TEXT,
    reference_number VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_invoices_rental_id FOREIGN KEY (rental_id) REFERENCES rentals(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoices_customer_id FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    
    -- Check constraints
    CONSTRAINT chk_invoice_status CHECK (status IN ('PENDING', 'SENT', 'PAID', 'OVERDUE', 'CANCELLED'))
);

-- ==============================================
-- GENERAL_LEDGER TABLE
-- ==============================================
CREATE TABLE IF NOT EXISTS general_ledger (
    id SERIAL PRIMARY KEY,
    transaction_type VARCHAR(50) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    account_code VARCHAR(100) NOT NULL,
    account_name VARCHAR(200) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    description TEXT,
    debit_amount DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (debit_amount >= 0),
    credit_amount DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (credit_amount >= 0),
    reference_id BIGINT,
    reference_type VARCHAR(50),
    document_number VARCHAR(100),
    reconciled BOOLEAN DEFAULT FALSE,
    reconciled_at TIMESTAMP,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Check constraints
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('REVENUE', 'EXPENSE', 'ASSET', 'LIABILITY', 'EQUITY', 'TRANSFER')),
    CONSTRAINT chk_account_type CHECK (account_type IN (
        'CASH_ASSET', 'BANK_ASSET', 'ACCOUNTS_RECEIVABLE', 'INVENTORY_ASSET', 'FIXED_ASSET',
        'ACCOUNTS_PAYABLE', 'TAX_PAYABLE', 'SHORT_TERM_DEBT', 'LONG_TERM_DEBT',
        'CAPITAL', 'RETAINED_EARNINGS',
        'RENTAL_REVENUE', 'OTHER_REVENUE',
        'OPERATING_EXPENSE', 'MAINTENANCE_EXPENSE', 'ADMINISTRATIVE_EXPENSE', 
        'TAX_EXPENSE', 'FUEL_EXPENSE', 'INSURANCE_EXPENSE'
    ))
);

-- ==============================================
-- TAX_CALCULATIONS TABLE
-- ==============================================
CREATE TABLE IF NOT EXISTS tax_calculations (
    id SERIAL PRIMARY KEY,
    invoice_id BIGINT,
    payment_id BIGINT,
    tax_type VARCHAR(50) NOT NULL,
    taxable_amount DECIMAL(12,2) NOT NULL CHECK (taxable_amount >= 0),
    tax_rate DECIMAL(5,4) NOT NULL CHECK (tax_rate >= 0 AND tax_rate <= 1),
    tax_amount DECIMAL(12,2) NOT NULL CHECK (tax_amount >= 0),
    calculation_date TIMESTAMP NOT NULL,
    calculation_details TEXT,
    is_reported BOOLEAN DEFAULT FALSE,
    reported_at TIMESTAMP,
    tax_period VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_tax_calculations_invoice_id FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE SET NULL,
    CONSTRAINT fk_tax_calculations_payment_id FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE SET NULL,
    
    -- Check constraints
    CONSTRAINT chk_tax_type CHECK (tax_type IN ('VAT', 'CORPORATE_TAX', 'WITHHOLDING_TAX', 'STAMP_TAX', 'MUNICIPAL_TAX'))
);

-- ==============================================
-- INDEXES FOR PERFORMANCE
-- ==============================================

-- Invoices indexes
CREATE INDEX IF NOT EXISTS idx_invoices_rental_id ON invoices(rental_id);
CREATE INDEX IF NOT EXISTS idx_invoices_customer_id ON invoices(customer_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices(status);
CREATE INDEX IF NOT EXISTS idx_invoices_due_date ON invoices(due_date);
CREATE INDEX IF NOT EXISTS idx_invoices_issue_date ON invoices(issue_date);
CREATE INDEX IF NOT EXISTS idx_invoices_invoice_number ON invoices(invoice_number);

-- General Ledger indexes
CREATE INDEX IF NOT EXISTS idx_general_ledger_transaction_date ON general_ledger(transaction_date);
CREATE INDEX IF NOT EXISTS idx_general_ledger_account_type ON general_ledger(account_type);
CREATE INDEX IF NOT EXISTS idx_general_ledger_transaction_type ON general_ledger(transaction_type);
CREATE INDEX IF NOT EXISTS idx_general_ledger_reference ON general_ledger(reference_type, reference_id);
CREATE INDEX IF NOT EXISTS idx_general_ledger_reconciled ON general_ledger(reconciled);

-- Tax Calculations indexes
CREATE INDEX IF NOT EXISTS idx_tax_calculations_invoice_id ON tax_calculations(invoice_id);
CREATE INDEX IF NOT EXISTS idx_tax_calculations_payment_id ON tax_calculations(payment_id);
CREATE INDEX IF NOT EXISTS idx_tax_calculations_tax_type ON tax_calculations(tax_type);
CREATE INDEX IF NOT EXISTS idx_tax_calculations_tax_period ON tax_calculations(tax_period);
CREATE INDEX IF NOT EXISTS idx_tax_calculations_calculation_date ON tax_calculations(calculation_date);
CREATE INDEX IF NOT EXISTS idx_tax_calculations_is_reported ON tax_calculations(is_reported);

-- ==============================================
-- COMMENTS FOR DOCUMENTATION
-- ==============================================

COMMENT ON TABLE invoices IS 'Invoice records for rental transactions';
COMMENT ON COLUMN invoices.invoice_number IS 'Unique invoice number';
COMMENT ON COLUMN invoices.subtotal IS 'Amount before tax';
COMMENT ON COLUMN invoices.tax_rate IS 'Tax rate as decimal (0.18 for 18%)';
COMMENT ON COLUMN invoices.tax_amount IS 'Calculated tax amount';
COMMENT ON COLUMN invoices.total_amount IS 'Final amount including tax';

COMMENT ON TABLE general_ledger IS 'General ledger entries for accounting system';
COMMENT ON COLUMN general_ledger.debit_amount IS 'Debit amount for this entry';
COMMENT ON COLUMN general_ledger.credit_amount IS 'Credit amount for this entry';
COMMENT ON COLUMN general_ledger.reference_type IS 'Type of reference (PAYMENT, INVOICE, etc.)';
COMMENT ON COLUMN general_ledger.reference_id IS 'ID of the referenced entity';

COMMENT ON TABLE tax_calculations IS 'Tax calculation records for compliance and reporting';
COMMENT ON COLUMN tax_calculations.taxable_amount IS 'Amount subject to tax';
COMMENT ON COLUMN tax_calculations.tax_rate IS 'Tax rate as decimal';
COMMENT ON COLUMN tax_calculations.tax_amount IS 'Calculated tax amount';
COMMENT ON COLUMN tax_calculations.tax_period IS 'Tax period in YYYY-MM format';

-- ==============================================
-- SAMPLE DATA (Optional - for testing)
-- ==============================================

-- Sample invoice data
INSERT INTO invoices (rental_id, customer_id, invoice_number, issue_date, due_date, subtotal, tax_rate, tax_amount, total_amount, status) VALUES
(1, 1, 'INV-20231201-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days', 1000.00, 0.18, 180.00, 1180.00, 'SENT'),
(2, 2, 'INV-20231201-002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days', 1500.00, 0.18, 270.00, 1770.00, 'PENDING')
ON CONFLICT DO NOTHING;

-- Sample general ledger entries
INSERT INTO general_ledger (transaction_type, account_type, account_code, account_name, transaction_date, description, debit_amount, credit_amount, document_number) VALUES
('REVENUE', 'CASH_ASSET', '1001', 'Kasa', CURRENT_TIMESTAMP, 'Test revenue entry', 1000.00, 0, 'GL-TEST-001'),
('REVENUE', 'RENTAL_REVENUE', '4001', 'Kiralama Geliri', CURRENT_TIMESTAMP, 'Test revenue entry', 0, 1000.00, 'GL-TEST-001')
ON CONFLICT DO NOTHING;

-- ==============================================
-- UPDATE TRIGGERS FOR UPDATED_DATE
-- ==============================================

-- Function to update updated_date
CREATE OR REPLACE FUNCTION update_updated_date_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_date = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for updated_date
DROP TRIGGER IF EXISTS update_invoices_updated_date ON invoices;
CREATE TRIGGER update_invoices_updated_date 
    BEFORE UPDATE ON invoices 
    FOR EACH ROW EXECUTE FUNCTION update_updated_date_column();

DROP TRIGGER IF EXISTS update_general_ledger_updated_date ON general_ledger;
CREATE TRIGGER update_general_ledger_updated_date 
    BEFORE UPDATE ON general_ledger 
    FOR EACH ROW EXECUTE FUNCTION update_updated_date_column();

DROP TRIGGER IF EXISTS update_tax_calculations_updated_date ON tax_calculations;
CREATE TRIGGER update_tax_calculations_updated_date 
    BEFORE UPDATE ON tax_calculations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_date_column();




