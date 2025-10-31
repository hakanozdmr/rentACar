-- Financial Management Sample Data
-- This script populates financial tables with realistic sample data

-- ==============================================
-- SAMPLE PAYMENTS DATA
-- ==============================================

-- Insert sample payments for existing rentals
INSERT INTO payments (rental_id, customer_id, amount, method, status, paid_at, due_date, transaction_id, notes, created_date) VALUES
-- Completed payments
(1, 1, 1180.00, 'CREDIT_CARD', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 'TXN-CC-20231201-001', 'Kredi kartı ile ödeme', CURRENT_TIMESTAMP - INTERVAL '2 days'),
(2, 2, 1770.00, 'BANK_TRANSFER', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '3 days', 'TXN-BT-20231202-001', 'Banka havalesi ile ödeme', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(3, 3, 850.00, 'CASH', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '7 days', 'TXN-CASH-20231130-001', 'Nakit ödeme', CURRENT_TIMESTAMP - INTERVAL '3 days'),

-- Pending payments
(4, 4, 1200.00, 'CREDIT_CARD', 'PENDING', NULL, CURRENT_TIMESTAMP + INTERVAL '5 days', NULL, 'Bekleyen ödeme - Kredi kartı', CURRENT_TIMESTAMP),
(5, 5, 2100.00, 'BANK_TRANSFER', 'PENDING', NULL, CURRENT_TIMESTAMP + INTERVAL '3 days', NULL, 'Bekleyen ödeme - Havale', CURRENT_TIMESTAMP),

-- Failed payments
(6, 6, 900.00, 'CREDIT_CARD', 'FAILED', NULL, CURRENT_TIMESTAMP + INTERVAL '2 days', 'TXN-CC-FAILED-001', 'Kart limiti yetersiz', CURRENT_TIMESTAMP - INTERVAL '1 day'),

-- Partial payments
(7, 7, 500.00, 'CREDIT_CARD', 'PARTIAL', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '5 days', 'TXN-CC-PARTIAL-001', 'Kısmi ödeme - Kalan: 800 TL', CURRENT_TIMESTAMP - INTERVAL '1 day'),

-- Overdue payments
(8, 8, 1500.00, 'BANK_TRANSFER', 'PENDING', NULL, CURRENT_TIMESTAMP - INTERVAL '10 days', NULL, 'Vadesi geçen ödeme', CURRENT_TIMESTAMP - INTERVAL '15 days'),
(9, 9, 950.00, 'ONLINE_BANKING', 'PENDING', NULL, CURRENT_TIMESTAMP - INTERVAL '5 days', NULL, 'Vadesi geçen ödeme', CURRENT_TIMESTAMP - INTERVAL '8 days')

ON CONFLICT DO NOTHING;

-- ==============================================
-- SAMPLE INVOICES DATA
-- ==============================================

-- Insert sample invoices
INSERT INTO invoices (rental_id, customer_id, invoice_number, issue_date, due_date, subtotal, tax_rate, tax_amount, total_amount, status, paid_at, payment_terms, notes, reference_number, created_date) VALUES

-- Paid invoices
(1, 1, 'INV-20231201-001', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 1000.00, 0.18, 180.00, 1180.00, 'PAID', CURRENT_TIMESTAMP - INTERVAL '2 days', '30 gün vadeli', 'Araç kiralama bedeli', 'REF-INV-001', CURRENT_TIMESTAMP - INTERVAL '10 days'),
(2, 2, 'INV-20231202-002', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 1500.00, 0.18, 270.00, 1770.00, 'PAID', CURRENT_TIMESTAMP - INTERVAL '1 day', '30 gün vadeli', 'Araç kiralama bedeli', 'REF-INV-002', CURRENT_TIMESTAMP - INTERVAL '8 days'),
(3, 3, 'INV-20231203-003', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '7 days', 720.34, 0.18, 129.66, 850.00, 'PAID', CURRENT_TIMESTAMP - INTERVAL '3 days', '30 gün vadeli', 'Araç kiralama bedeli', 'REF-INV-003', CURRENT_TIMESTAMP - INTERVAL '5 days'),

-- Pending invoices
(4, 4, 'INV-20231204-004', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP + INTERVAL '5 days', 1016.95, 0.18, 183.05, 1200.00, 'SENT', NULL, '30 gün vadeli', 'Araç kiralama bedeli', 'REF-INV-004', CURRENT_TIMESTAMP - INTERVAL '3 days'),
(5, 5, 'INV-20231205-005', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP + INTERVAL '3 days', 1779.66, 0.18, 320.34, 2100.00, 'PENDING', NULL, '30 gün vadeli', 'Araç kiralama bedeli', 'REF-INV-005', CURRENT_TIMESTAMP - INTERVAL '2 days'),

-- Overdue invoices
(6, 6, 'INV-20231125-006', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 762.71, 0.18, 137.29, 900.00, 'OVERDUE', NULL, '30 gün vadeli', 'Araç kiralama bedeli - Vadesi geçti', 'REF-INV-006', CURRENT_TIMESTAMP - INTERVAL '15 days'),
(7, 7, 'INV-20231120-007', CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 847.46, 0.18, 152.54, 1000.00, 'OVERDUE', NULL, '30 gün vadeli', 'Araç kiralama bedeli - Vadesi geçti', 'REF-INV-007', CURRENT_TIMESTAMP - INTERVAL '20 days'),
(8, 8, 'INV-20231115-008', CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP - INTERVAL '10 days', 1271.19, 0.18, 228.81, 1500.00, 'OVERDUE', NULL, '30 gün vadeli', 'Araç kiralama bedeli - Vadesi geçti', 'REF-INV-008', CURRENT_TIMESTAMP - INTERVAL '25 days')

ON CONFLICT DO NOTHING;

-- ==============================================
-- SAMPLE GENERAL LEDGER DATA
-- ==============================================

-- Insert sample general ledger entries
INSERT INTO general_ledger (transaction_type, account_type, account_code, account_name, transaction_date, description, debit_amount, credit_amount, reference_id, reference_type, document_number, reconciled, created_date) VALUES

-- Revenue entries from completed rentals
('REVENUE', 'CASH_ASSET', '1001', 'Ana Kasa', CURRENT_TIMESTAMP - INTERVAL '2 days', 'Kiralama geliri - Rental ID: 1', 1180.00, 0, 1, 'RENTAL', 'GL-RENT-001', true, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('REVENUE', 'RENTAL_REVENUE', '4001', 'Kiralama Geliri', CURRENT_TIMESTAMP - INTERVAL '2 days', 'Kiralama geliri - Rental ID: 1', 0, 1180.00, 1, 'RENTAL', 'GL-RENT-001', true, CURRENT_TIMESTAMP - INTERVAL '2 days'),

('REVENUE', 'BANK_ASSET', '1002', 'Ana Banka Hesabı', CURRENT_TIMESTAMP - INTERVAL '1 day', 'Kiralama geliri - Rental ID: 2', 1770.00, 0, 2, 'RENTAL', 'GL-RENT-002', true, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('REVENUE', 'RENTAL_REVENUE', '4001', 'Kiralama Geliri', CURRENT_TIMESTAMP - INTERVAL '1 day', 'Kiralama geliri - Rental ID: 2', 0, 1770.00, 2, 'RENTAL', 'GL-RENT-002', true, CURRENT_TIMESTAMP - INTERVAL '1 day'),

-- Expense entries
('EXPENSE', 'MAINTENANCE_EXPENSE', '5001', 'Bakım ve Onarım Gideri', CURRENT_TIMESTAMP - INTERVAL '5 days', 'Araç periyodik bakım', 450.00, 0, NULL, 'EXPENSE', 'GL-EXP-001', false, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('EXPENSE', 'CASH_ASSET', '1001', 'Ana Kasa', CURRENT_TIMESTAMP - INTERVAL '5 days', 'Araç periyodik bakım', 0, 450.00, NULL, 'EXPENSE', 'GL-EXP-001', false, CURRENT_TIMESTAMP - INTERVAL '5 days'),

('EXPENSE', 'FUEL_EXPENSE', '5002', 'Yakıt Gideri', CURRENT_TIMESTAMP - INTERVAL '3 days', 'Araç yakıt ikmali', 1200.00, 0, NULL, 'EXPENSE', 'GL-EXP-002', false, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('EXPENSE', 'BANK_ASSET', '1002', 'Ana Banka Hesabı', CURRENT_TIMESTAMP - INTERVAL '3 days', 'Araç yakıt ikmali', 0, 1200.00, NULL, 'EXPENSE', 'GL-EXP-002', false, CURRENT_TIMESTAMP - INTERVAL '3 days'),

-- Tax entries
('EXPENSE', 'TAX_PAYABLE', '2001', 'KDV Borcu', CURRENT_TIMESTAMP - INTERVAL '1 day', 'KDV Tahsilatı - Invoice ID: 1', 0, 180.00, 1, 'INVOICE', 'GL-TAX-001', false, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('EXPENSE', 'CASH_ASSET', '1001', 'Ana Kasa', CURRENT_TIMESTAMP - INTERVAL '1 day', 'KDV Tahsilatı - Invoice ID: 1', 180.00, 0, 1, 'INVOICE', 'GL-TAX-001', false, CURRENT_TIMESTAMP - INTERVAL '1 day'),

-- Accounts receivable entries for pending invoices
('EXPENSE', 'ACCOUNTS_RECEIVABLE', '1101', 'Müşteri Alacakları', CURRENT_TIMESTAMP - INTERVAL '3 days', 'Fatura kesimi - Invoice ID: 4', 1200.00, 0, 4, 'INVOICE', 'GL-AR-001', false, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('REVENUE', 'RENTAL_REVENUE', '4001', 'Kiralama Geliri', CURRENT_TIMESTAMP - INTERVAL '3 days', 'Fatura kesimi - Invoice ID: 4', 0, 1200.00, 4, 'INVOICE', 'GL-AR-001', false, CURRENT_TIMESTAMP - INTERVAL '3 days')

ON CONFLICT DO NOTHING;

-- ==============================================
-- SAMPLE TAX CALCULATION DATA
-- ==============================================

-- Insert sample tax calculations
INSERT INTO tax_calculations (invoice_id, payment_id, tax_type, taxable_amount, tax_rate, tax_amount, calculation_date, calculation_details, is_reported, tax_period, created_date) VALUES

-- VAT calculations for paid invoices
(1, 1, 'VAT', 1000.00, 0.18, 180.00, CURRENT_TIMESTAMP - INTERVAL '10 days', 'KDV hesaplaması - Invoice: INV-20231201-001, Mükellef: RentACar Ltd.', true, '2023-12', CURRENT_TIMESTAMP - INTERVAL '10 days'),
(2, 2, 'VAT', 1500.00, 0.18, 270.00, CURRENT_TIMESTAMP - INTERVAL '8 days', 'KDV hesaplaması - Invoice: INV-20231202-002, Mükellef: RentACar Ltd.', true, '2023-12', CURRENT_TIMESTAMP - INTERVAL '8 days'),
(3, 3, 'VAT', 720.34, 0.18, 129.66, CURRENT_TIMESTAMP - INTERVAL '5 days', 'KDV hesaplaması - Invoice: INV-20231203-003, Mükellef: RentACar Ltd.', true, '2023-12', CURRENT_TIMESTAMP - INTERVAL '5 days'),

-- VAT calculations for pending invoices
(4, NULL, 'VAT', 1016.95, 0.18, 183.05, CURRENT_TIMESTAMP - INTERVAL '3 days', 'KDV hesaplaması - Invoice: INV-20231204-004, Mükellef: RentACar Ltd.', false, '2023-12', CURRENT_TIMESTAMP - INTERVAL '3 days'),
(5, NULL, 'VAT', 1779.66, 0.18, 320.34, CURRENT_TIMESTAMP - INTERVAL '2 days', 'KDV hesaplaması - Invoice: INV-20231205-005, Mükellef: RentACar Ltd.', false, '2023-12', CURRENT_TIMESTAMP - INTERVAL '2 days'),

-- Corporate tax calculation (monthly)
(NULL, NULL, 'CORPORATE_TAX', 4950.61, 0.20, 990.12, CURRENT_TIMESTAMP - INTERVAL '1 day', 'Kurumlar vergisi hesaplaması - Aralık 2023 dönemi', false, '2023-12', CURRENT_TIMESTAMP - INTERVAL '1 day'),

-- Withholding tax calculations
(1, NULL, 'WITHHOLDING_TAX', 1000.00, 0.015, 15.00, CURRENT_TIMESTAMP - INTERVAL '10 days', 'Stopaj hesaplaması - Müşteri: 1', false, '2023-12', CURRENT_TIMESTAMP - INTERVAL '10 days'),
(2, NULL, 'WITHHOLDING_TAX', 1500.00, 0.015, 22.50, CURRENT_TIMESTAMP - INTERVAL '8 days', 'Stopaj hesaplaması - Müşteri: 2', false, '2023-12', CURRENT_TIMESTAMP - INTERVAL '8 days'),

-- Stamp tax calculations
(1, NULL, 'STAMP_TAX', 1180.00, 0.0075, 8.85, CURRENT_TIMESTAMP - INTERVAL '10 days', 'Damga vergisi hesaplaması - Belge bedeli', false, '2023-12', CURRENT_TIMESTAMP - INTERVAL '10 days'),
(2, NULL, 'STAMP_TAX', 1770.00, 0.0075, 13.28, CURRENT_TIMESTAMP - INTERVAL '8 days', 'Damga vergisi hesaplaması - Belge bedeli', false, '2023-12', CURRENT_TIMESTAMP - INTERVAL '8 days')

ON CONFLICT DO NOTHING;

-- ==============================================
-- UPDATE SEQUENCES
-- ==============================================

-- Reset sequences to proper values
SELECT setval('invoices_id_seq', (SELECT MAX(id) FROM invoices));
SELECT setval('general_ledger_id_seq', (SELECT MAX(id) FROM general_ledger));
SELECT setval('tax_calculations_id_seq', (SELECT MAX(id) FROM tax_calculations));


