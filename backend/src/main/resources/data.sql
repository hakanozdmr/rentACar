-- Spring Boot için otomatik veri yükleme
-- uygulama başladığında bu dosya otomatik çalışacak

-- Foreign key constraints nedeniyle ters sırada temizleme
-- Önce bağımlı tabloları temizle

-- User roles tablosunu temizle (en bağımlı)
DELETE FROM user_roles;

-- Users tablosunu temizle
DELETE FROM users;

-- Roles tablosunu temizle
DELETE FROM roles;

-- Rentals tablosunu temizle
DELETE FROM rentals;

-- Customers tablosunu temizle  
DELETE FROM customers;

-- Cars tablosunu temizle
DELETE FROM cars;

-- Models tablosunu temizle
DELETE FROM models;

-- Brands tablosunu temizle ve verileri ekle (en bağımsız)
INSERT INTO brands (id, name, created_by, created_date) VALUES
(1, 'Toyota', 'system', CURRENT_TIMESTAMP),
(2, 'Ford', 'system', CURRENT_TIMESTAMP),
(3, 'BMW', 'system', CURRENT_TIMESTAMP),
(4, 'Mercedes-Benz', 'system', CURRENT_TIMESTAMP),
(5, 'Audi', 'system', CURRENT_TIMESTAMP),
(6, 'Volkswagen', 'system', CURRENT_TIMESTAMP),
(7, 'Honda', 'system', CURRENT_TIMESTAMP),
(8, 'Hyundai', 'system', CURRENT_TIMESTAMP),
(9, 'Renault', 'system', CURRENT_TIMESTAMP),
(10, 'Peugeot', 'system', CURRENT_TIMESTAMP);

-- Models tablosunu temizle ve verileri ekle
INSERT INTO models (id, name, brand_id, created_by, created_date) VALUES
(1, 'Corolla', 1, 'system', CURRENT_TIMESTAMP),
(2, 'Camry', 1, 'system', CURRENT_TIMESTAMP),
(3, 'RAV4', 1, 'system', CURRENT_TIMESTAMP),
(4, 'Focus', 2, 'system', CURRENT_TIMESTAMP),
(5, 'Mustang', 2, 'system', CURRENT_TIMESTAMP),
(6, 'Explorer', 2, 'system', CURRENT_TIMESTAMP),
(7, '3 Series', 3, 'system', CURRENT_TIMESTAMP),
(8, '5 Series', 3, 'system', CURRENT_TIMESTAMP),
(9, 'X3', 3, 'system', CURRENT_TIMESTAMP),
(10, 'C-Class', 4, 'system', CURRENT_TIMESTAMP),
(11, 'E-Class', 4, 'system', CURRENT_TIMESTAMP),
(12, 'S-Class', 4, 'system', CURRENT_TIMESTAMP),
(13, 'A3', 5, 'system', CURRENT_TIMESTAMP),
(14, 'A4', 5, 'system', CURRENT_TIMESTAMP),
(15, 'Q5', 5, 'system', CURRENT_TIMESTAMP),
(16, 'Golf', 6, 'system', CURRENT_TIMESTAMP),
(17, 'Passat', 6, 'system', CURRENT_TIMESTAMP),
(18, 'Polo', 6, 'system', CURRENT_TIMESTAMP),
(19, 'Civic', 7, 'system', CURRENT_TIMESTAMP),
(20, 'Accord', 7, 'system', CURRENT_TIMESTAMP),
(21, 'Elantra', 8, 'system', CURRENT_TIMESTAMP),
(22, 'Tucson', 8, 'system', CURRENT_TIMESTAMP),
(23, 'Clio', 9, 'system', CURRENT_TIMESTAMP),
(24, 'Megane', 9, 'system', CURRENT_TIMESTAMP),
(25, '208', 10, 'system', CURRENT_TIMESTAMP),
(26, '308', 10, 'system', CURRENT_TIMESTAMP);

-- Cars tablosunu temizle ve verileri ekle
INSERT INTO cars (id, plate, daily_price, model_year, state, model_id, created_by, created_date) VALUES
(1, '34ABC123', 450.00, 2023, 1, 1, 'system', CURRENT_TIMESTAMP),
(2, '06DEF456', 550.00, 2023, 1, 2, 'system', CURRENT_TIMESTAMP),
(3, '35GHI789', 600.00, 2023, 1, 3, 'system', CURRENT_TIMESTAMP),
(4, '16JKL012', 400.00, 2022, 2, 4, 'system', CURRENT_TIMESTAMP),
(5, '01MNO345', 800.00, 2023, 1, 5, 'system', CURRENT_TIMESTAMP),
(6, '07PQR678', 700.00, 2022, 1, 6, 'system', CURRENT_TIMESTAMP),
(7, '34STU901', 750.00, 2023, 1, 7, 'system', CURRENT_TIMESTAMP),
(8, '06VWX234', 850.00, 2023, 1, 8, 'system', CURRENT_TIMESTAMP),
(9, '35YZA567', 900.00, 2022, 2, 9, 'system', CURRENT_TIMESTAMP),
(10, '34BCD890', 800.00, 2023, 1, 10, 'system', CURRENT_TIMESTAMP),
(11, '06EFG123', 950.00, 2023, 1, 11, 'system', CURRENT_TIMESTAMP),
(12, '35HIJ456', 1200.00, 2023, 1, 12, 'system', CURRENT_TIMESTAMP),
(13, '16KLM789', 650.00, 2022, 1, 13, 'system', CURRENT_TIMESTAMP),
(14, '01NOP012', 750.00, 2023, 1, 14, 'system', CURRENT_TIMESTAMP),
(15, '07QRS345', 800.00, 2023, 3, 15, 'system', CURRENT_TIMESTAMP),
(16, '34TUV678', 400.00, 2022, 1, 16, 'system', CURRENT_TIMESTAMP),
(17, '06WXY901', 500.00, 2023, 1, 17, 'system', CURRENT_TIMESTAMP),
(18, '35ZAB234', 350.00, 2022, 1, 18, 'system', CURRENT_TIMESTAMP),
(19, '34CDE567', 450.00, 2023, 2, 19, 'system', CURRENT_TIMESTAMP),
(20, '06FGH890', 550.00, 2023, 1, 20, 'system', CURRENT_TIMESTAMP),
(21, '35IJK123', 400.00, 2022, 1, 21, 'system', CURRENT_TIMESTAMP),
(22, '16LMN456', 600.00, 2023, 1, 22, 'system', CURRENT_TIMESTAMP),
(23, '01OPQ789', 380.00, 2022, 1, 23, 'system', CURRENT_TIMESTAMP),
(24, '07RST012', 450.00, 2023, 1, 24, 'system', CURRENT_TIMESTAMP),
(25, '34UVW345', 400.00, 2022, 1, 25, 'system', CURRENT_TIMESTAMP),
(26, '06XYZ678', 480.00, 2023, 1, 26, 'system', CURRENT_TIMESTAMP);

-- Customers tablosunu temizle ve verileri ekle
INSERT INTO customers (id, first_name, last_name, street, zipcode, city, phone, email, date_of_birth, id_number, driver_license_number, created_by, created_date) VALUES
(1, 'Ahmet', 'Yılmaz', 'Atatürk Caddesi No:45 Daire:12', 34000, 'İstanbul', '+90 532 123 4567', 'ahmet.yilmaz@email.com', '1985-03-15', '12345678901', 'A1234567', 'system', CURRENT_TIMESTAMP),
(2, 'Ayşe', 'Kaya', 'Cumhuriyet Bulvarı No:78', 35200, 'İzmir', '+90 555 987 6543', 'ayse.kaya@email.com', '1990-07-22', '23456789012', 'B2345678', 'system', CURRENT_TIMESTAMP),
(3, 'Mehmet', 'Demir', 'Millet Caddesi No:123', 35000, 'İzmir', '+90 533 456 7890', 'mehmet.demir@email.com', '1982-11-08', '34567890123', 'C3456789', 'system', CURRENT_TIMESTAMP),
(4, 'Fatma', 'Özkan', 'Şehitler Caddesi No:67', 16100, 'Bursa', '+90 534 567 8901', 'fatma.ozkan@email.com', '1988-05-30', '45678901234', 'D4567890', 'system', CURRENT_TIMESTAMP),
(5, 'Ali', 'Şahin', 'Gazi Mustafa Kemal Bulvarı No:89', 42000, 'Konya', '+90 535 678 9012', 'ali.sahin@email.com', '1983-09-14', '56789012345', 'E5678901', 'system', CURRENT_TIMESTAMP),
(6, 'Zeynep', 'Aydın', 'İnönü Caddesi No:234', 35000, 'Antalya', '+90 536 789 0123', 'zeynep.aydin@email.com', '1992-12-03', '67890123456', 'F6789012', 'system', CURRENT_TIMESTAMP),
(7, 'Mustafa', 'Çelik', 'Sakarya Caddesi No:145', 55000, 'Samsun', '+90 537 890 1234', 'mustafa.celik@email.com', '1987-04-18', '78901234567', 'G7890123', 'system', CURRENT_TIMESTAMP),
(8, 'Elif', 'Kurt', 'Mimar Sinan Caddesi No:56', 26000, 'Eskişehir', '+90 538 901 2345', 'elif.kurt@email.com', '1991-08-25', '89012345678', 'H8901234', 'system', CURRENT_TIMESTAMP),
(9, 'Hakan', 'Arslan', 'Özgürlük Caddesi No:189', 28000, 'Giresun', '+90 539 012 3456', 'hakan.arslan@email.com', '1986-01-12', '90123456789', 'I9012345', 'system', CURRENT_TIMESTAMP),
(10, 'Selin', 'Özdemir', 'Cumhuriyet Mahallesi No:78', 38000, 'Kayseri', '+90 540 123 4567', 'selin.ozdemir@email.com', '1993-06-07', '01234567890', 'J0123456', 'system', CURRENT_TIMESTAMP);

-- Rentals tablosunu temizle ve verileri ekle (now includes merged reservation fields)
INSERT INTO rentals (id, start, "end", car_id, customer_id, extra_costs, note, status, total_amount, special_requests, confirmed_at, created_by, created_date) VALUES
(1, '2024-01-15', '2024-01-20', 4, 1, 150, 'Havalimanı teslim', 'COMPLETED', 2400.00, 'Havalimanı pick-up ve drop-off', '2024-01-14 10:00:00', 'system', CURRENT_TIMESTAMP),
(2, '2024-01-18', '2024-01-25', 9, 2, 0, NULL, 'COMPLETED', 5600.00, NULL, '2024-01-17 14:30:00', 'system', CURRENT_TIMESTAMP),
(3, '2024-02-01', '2024-02-03', 19, 3, 75, 'Şehir merkezi teslim', 'COMPLETED', 975.00, 'Şehir merkezi teslim ve teslim alma', '2024-01-31 16:00:00', 'system', CURRENT_TIMESTAMP),
(4, '2024-02-05', '2024-02-12', 1, 4, 0, NULL, 'COMPLETED', 3600.00, NULL, '2024-02-04 12:00:00', 'system', CURRENT_TIMESTAMP),
(5, '2024-02-08', '2024-02-15', 7, 5, 200, 'VIP müşteri', 'COMPLETED', 6000.00, 'VIP servis, şoförlü araç', '2024-02-07 09:00:00', 'system', CURRENT_TIMESTAMP),
(6, '2024-02-10', '2024-02-14', 11, 6, 0, NULL, 'COMPLETED', 4750.00, NULL, '2024-02-09 11:30:00', 'system', CURRENT_TIMESTAMP),
(7, '2024-02-12', '2024-02-18', 3, 7, 100, 'Uzun mesafe kullanımı', 'COMPLETED', 4200.00, 'Uzun mesafe kullanım için ekstra yakıt', '2024-02-11 08:45:00', 'system', CURRENT_TIMESTAMP),
(8, '2024-02-15', '2024-02-20', 6, 8, 0, NULL, 'COMPLETED', 3500.00, NULL, '2024-02-14 13:15:00', 'system', CURRENT_TIMESTAMP),
(9, '2024-02-18', '2024-02-25', 10, 9, 125, 'Ek sigorta dahil', 'COMPLETED', 6500.00, 'Ek sigorta dahil, tam koruma paketi', '2024-02-17 10:20:00', 'system', CURRENT_TIMESTAMP),
(10, '2024-02-20', '2024-02-28', 14, 10, 0, NULL, 'COMPLETED', 6750.00, NULL, '2024-02-19 15:45:00', 'system', CURRENT_TIMESTAMP);

-- Roles tablosunu temizle ve verileri ekle
INSERT INTO roles (id, name, description, created_by, created_date) VALUES
(1, 'ADMIN', 'Admin user with full access', 'system', CURRENT_TIMESTAMP),
(2, 'USER', 'Regular user', 'system', CURRENT_TIMESTAMP),
(3, 'EMPLOYEE', 'Employee user', 'system', CURRENT_TIMESTAMP);

-- Users tablosunu temizle ve verileri ekle
INSERT INTO users (id, username, email, password, first_name, last_name, enabled, created_by, created_date) VALUES
(1, 'admin', 'admin@rentacar.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfF7zBixm0P7K9sZGzJ0I2a2', 'Admin', 'User', true, 'system', CURRENT_TIMESTAMP);

-- User roles tablosunu temizle ve verileri ekle
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1);

-- Sequence'ları güncelle
SELECT setval('brands_id_seq', (SELECT MAX(id) FROM brands));
SELECT setval('models_id_seq', (SELECT MAX(id) FROM models));
SELECT setval('cars_id_seq', (SELECT MAX(id) FROM cars));
SELECT setval('customers_id_seq', (SELECT MAX(id) FROM customers));
SELECT setval('rentals_id_seq', (SELECT MAX(id) FROM rentals));
SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

