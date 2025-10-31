-- RentACar Projesi Örnek Veriler
-- PostgreSQL için hazırlanmıştır

-- Brands (Markalar) verileri
INSERT INTO brands (id, name, created_by, created_date) VALUES
(1, 'Toyota', 'system', NOW()),
(2, 'Ford', 'system', NOW()),
(3, 'BMW', 'system', NOW()),
(4, 'Mercedes-Benz', 'system', NOW()),
(5, 'Audi', 'system', NOW()),
(6, 'Volkswagen', 'system', NOW()),
(7, 'Honda', 'system', NOW()),
(8, 'Hyundai', 'system', NOW()),
(9, 'Renault', 'system', NOW()),
(10, 'Peugeot', 'system', NOW());

-- Models (Modeller) verileri
INSERT INTO models (id, name, brand_id, created_by, created_date) VALUES
(1, 'Corolla', 1, 'system', NOW()),
(2, 'Camry', 1, 'system', NOW()),
(3, 'RAV4', 1, 'system', NOW()),
(4, 'Focus', 2, 'system', NOW()),
(5, 'Mustang', 2, 'system', NOW()),
(6, 'Explorer', 2, 'system', NOW()),
(7, '3 Series', 3, 'system', NOW()),
(8, '5 Series', 3, 'system', NOW()),
(9, 'X3', 3, 'system', NOW()),
(10, 'C-Class', 4, 'system', NOW()),
(11, 'E-Class', 4, 'system', NOW()),
(12, 'S-Class', 4, 'system', NOW()),
(13, 'A3', 5, 'system', NOW()),
(14, 'A4', 5, 'system', NOW()),
(15, 'Q5', 5, 'system', NOW()),
(16, 'Golf', 6, 'system', NOW()),
(17, 'Passat', 6, 'system', NOW()),
(18, 'Polo', 6, 'system', NOW()),
(19, 'Civic', 7, 'system', NOW()),
(20, 'Accord', 7, 'system', NOW()),
(21, 'Elantra', 8, 'system', NOW()),
(22, 'Tucson', 8, 'system', NOW()),
(23, 'Clio', 9, 'system', NOW()),
(24, 'Megane', 9, 'system', NOW()),
(25, '208', 10, 'system', NOW()),
(26, '308', 10, 'system', NOW());

-- Cars (Araçlar) verileri
INSERT INTO cars (id, plate, daily_price, model_year, state, model_id, created_by, created_date) VALUES
(1, '34ABC123', 450.00, 2023, 1, 1, 'system', NOW()),
(2, '06DEF456', 550.00, 2023, 1, 2, 'system', NOW()),
(3, '35GHI789', 600.00, 2023, 1, 3, 'system', NOW()),
(4, '16JKL012', 400.00, 2022, 2, 4, 'system', NOW()),
(5, '01MNO345', 800.00, 2023, 1, 5, 'system', NOW()),
(6, '07PQR678', 700.00, 2022, 1, 6, 'system', NOW()),
(7, '34STU901', 750.00, 2023, 1, 7, 'system', NOW()),
(8, '06VWX234', 850.00, 2023, 1, 8, 'system', NOW()),
(9, '35YZA567', 900.00, 2022, 2, 9, 'system', NOW()),
(10, '34BCD890', 800.00, 2023, 1, 10, 'system', NOW()),
(11, '06EFG123', 950.00, 2023, 1, 11, 'system', NOW()),
(12, '35HIJ456', 1200.00, 2023, 1, 12, 'system', NOW()),
(13, '16KLM789', 650.00, 2022, 1, 13, 'system', NOW()),
(14, '01NOP012', 750.00, 2023, 1, 14, 'system', NOW()),
(15, '07QRS345', 800.00, 2023, 3, 15, 'system', NOW()),
(16, '34TUV678', 400.00, 2022, 1, 16, 'system', NOW()),
(17, '06WXY901', 500.00, 2023, 1, 17, 'system', NOW()),
(18, '35ZAB234', 350.00, 2022, 1, 18, 'system', NOW()),
(19, '34CDE567', 450.00, 2023, 2, 19, 'system', NOW()),
(20, '06FGH890', 550.00, 2023, 1, 20, 'system', NOW()),
(21, '35IJK123', 400.00, 2022, 1, 21, 'system', NOW()),
(22, '16LMN456', 600.00, 2023, 1, 22, 'system', NOW()),
(23, '01OPQ789', 380.00, 2022, 1, 23, 'system', NOW()),
(24, '07RST012', 450.00, 2023, 1, 24, 'system', NOW()),
(25, '34UVW345', 400.00, 2022, 1, 25, 'system', NOW()),
(26, '06XYZ678', 480.00, 2023, 1, 26, 'system', NOW());

-- Customers (Müşteriler) verileri
INSERT INTO customers (id, first_name, last_name, street, zipcode, city, phone, email, date_of_birth, id_number, driver_license_number, created_by, created_date) VALUES
(1, 'Ahmet', 'Yılmaz', 'Atatürk Caddesi No:45 Daire:12', 34000, 'İstanbul', '+90 532 123 4567', 'ahmet.yilmaz@email.com', '1985-03-15', '12345678901', 'A1234567', 'system', NOW()),
(2, 'Ayşe', 'Kaya', 'Cumhuriyet Bulvarı No:78', 35200, 'İzmir', '+90 555 987 6543', 'ayse.kaya@email.com', '1990-07-22', '23456789012', 'B2345678', 'system', NOW()),
(3, 'Mehmet', 'Demir', 'Millet Caddesi No:123', 35000, 'İzmir', '+90 533 456 7890', 'mehmet.demir@email.com', '1982-11-08', '34567890123', 'C3456789', 'system', NOW()),
(4, 'Fatma', 'Özkan', 'Şehitler Caddesi No:67', 16100, 'Bursa', '+90 534 567 8901', 'fatma.ozkan@email.com', '1988-05-30', '45678901234', 'D4567890', 'system', NOW()),
(5, 'Ali', 'Şahin', 'Gazi Mustafa Kemal Bulvarı No:89', 42000, 'Konya', '+90 535 678 9012', 'ali.sahin@email.com', '1983-09-14', '56789012345', 'E5678901', 'system', NOW()),
(6, 'Zeynep', 'Aydın', 'İnönü Caddesi No:234', 35000, 'Antalya', '+90 536 789 0123', 'zeynep.aydin@email.com', '1992-12-03', '67890123456', 'F6789012', 'system', NOW()),
(7, 'Mustafa', 'Çelik', 'Sakarya Caddesi No:145', 55000, 'Samsun', '+90 537 890 1234', 'mustafa.celik@email.com', '1987-04-18', '78901234567', 'G7890123', 'system', NOW()),
(8, 'Elif', 'Kurt', 'Mimar Sinan Caddesi No:56', 26000, 'Eskişehir', '+90 538 901 2345', 'elif.kurt@email.com', '1991-08-25', '89012345678', 'H8901234', 'system', NOW()),
(9, 'Hakan', 'Arslan', 'Özgürlük Caddesi No:189', 28000, 'Giresun', '+90 539 012 3456', 'hakan.arslan@email.com', '1986-01-12', '90123456789', 'I9012345', 'system', NOW()),
(10, 'Selin', 'Özdemir', 'Cumhuriyet Mahallesi No:78', 38000, 'Kayseri', '+90 540 123 4567', 'selin.ozdemir@email.com', '1993-06-07', '01234567890', 'J0123456', 'system', NOW());

-- Rentals (Kiralamalar) verileri
INSERT INTO rentals (id, start, "end", car_id, customer_id, extra_costs, note, created_by, created_date) VALUES
(1, '2024-01-15', '2024-01-20', 4, 1, 150.00, 'Havalimanı teslim', 'system', NOW()),
(2, '2024-01-18', '2024-01-25', 9, 2, 0.00, NULL, 'system', NOW()),
(3, '2024-02-01', '2024-02-03', 19, 3, 75.00, 'Şehir merkezi teslim', 'system', NOW()),
(4, '2024-02-05', '2024-02-12', 1, 4, 0.00, NULL, 'system', NOW()),
(5, '2024-02-08', '2024-02-15', 7, 5, 200.00, 'VIP müşteri', 'system', NOW()),
(6, '2024-02-10', '2024-02-14', 11, 6, 0.00, NULL, 'system', NOW()),
(7, '2024-02-12', '2024-02-18', 3, 7, 100.00, 'Uzun mesafe kullanımı', 'system', NOW()),
(8, '2024-02-15', '2024-02-20', 6, 8, 0.00, NULL, 'system', NOW()),
(9, '2024-02-18', '2024-02-25', 10, 9, 125.00, 'Ek sigorta dahil', 'system', NOW()),
(10, '2024-02-20', '2024-02-28', 14, 10, 0.00, NULL, 'system', NOW());

-- Sequence'ları güncelle (ID'ler manuel girildiği için)
SELECT setval('brands_id_seq', (SELECT MAX(id) FROM brands));
SELECT setval('models_id_seq', (SELECT MAX(id) FROM models));
SELECT setval('cars_id_seq', (SELECT MAX(id) FROM cars));
SELECT setval('customers_id_seq', (SELECT MAX(id) FROM customers));
SELECT setval('rentals_id_seq', (SELECT MAX(id) FROM rentals));

