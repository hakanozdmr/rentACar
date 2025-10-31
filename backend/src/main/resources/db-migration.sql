-- Araç Yönetimi Geliştirmeleri için Database Migration Script

-- Cars tablosuna yeni kolonlar ekleme
ALTER TABLE cars ADD COLUMN IF NOT EXISTS mileage BIGINT;
ALTER TABLE cars ADD COLUMN IF NOT EXISTS fuel_type VARCHAR(20);
ALTER TABLE cars ADD COLUMN IF NOT EXISTS transmission VARCHAR(20);
ALTER TABLE cars ADD COLUMN IF NOT EXISTS segment VARCHAR(20);
ALTER TABLE cars ADD COLUMN IF NOT EXISTS color VARCHAR(50);
ALTER TABLE cars ADD COLUMN IF NOT EXISTS last_maintenance_date DATE;
ALTER TABLE cars ADD COLUMN IF NOT EXISTS next_maintenance_date DATE;
ALTER TABLE cars ADD COLUMN IF NOT EXISTS insurance_expiry_date DATE;
ALTER TABLE cars ADD COLUMN IF NOT EXISTS insurance_company VARCHAR(100);
ALTER TABLE cars ADD COLUMN IF NOT EXISTS gps_latitude DOUBLE PRECISION;
ALTER TABLE cars ADD COLUMN IF NOT EXISTS gps_longitude DOUBLE PRECISION;
ALTER TABLE cars ADD COLUMN IF NOT EXISTS last_location_update TIMESTAMP;

-- Car features tablosu (ElementCollection için)
CREATE TABLE IF NOT EXISTS car_features (
    car_id BIGINT NOT NULL,
    feature VARCHAR(100) NOT NULL,
    PRIMARY KEY (car_id, feature),
    FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE
);

-- Car images tablosu (ElementCollection için)
CREATE TABLE IF NOT EXISTS car_images (
    car_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (car_id, image_url),
    FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE
);

-- Maintenance records tablosu
CREATE TABLE IF NOT EXISTS maintenance_records (
    id BIGSERIAL PRIMARY KEY,
    car_id BIGINT NOT NULL,
    maintenance_date DATE NOT NULL,
    maintenance_type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    mileage_at_service BIGINT NOT NULL,
    service_provider VARCHAR(200) NOT NULL,
    created_by VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_date TIMESTAMP,
    FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE
);

-- GPS Location History tablosu
CREATE TABLE IF NOT EXISTS gps_location_history (
    id BIGSERIAL PRIMARY KEY,
    car_id BIGINT NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    address VARCHAR(500),
    speed DECIMAL(5,2),
    is_online BOOLEAN DEFAULT true,
    battery_level INTEGER,
    source VARCHAR(50) DEFAULT 'GPS_DEVICE',
    created_by VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_date TIMESTAMP,
    FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE
);

-- GPS location history için index ekleme (performans için)
CREATE INDEX IF NOT EXISTS idx_gps_location_history_car_id_recorded_at 
ON gps_location_history(car_id, recorded_at DESC);

CREATE INDEX IF NOT EXISTS idx_gps_location_history_recorded_at 
ON gps_location_history(recorded_at DESC);

-- Örnek verileri güncelleme
UPDATE cars SET 
    mileage = CASE 
        WHEN id = 1 THEN 45000
        WHEN id = 2 THEN 32000
        WHEN id = 3 THEN 28000
        WHEN id = 4 THEN 55000
        WHEN id = 5 THEN 20000
        WHEN id = 6 THEN 41000
        WHEN id = 7 THEN 35000
        WHEN id = 8 THEN 25000
        WHEN id = 9 THEN 48000
        WHEN id = 10 THEN 38000
        WHEN id = 11 THEN 22000
        WHEN id = 12 THEN 15000
        WHEN id = 13 THEN 42000
        WHEN id = 14 THEN 31000
        WHEN id = 15 THEN 60000
        WHEN id = 16 THEN 45000
        WHEN id = 17 THEN 39000
        WHEN id = 18 THEN 52000
        WHEN id = 19 THEN 47000
        WHEN id = 20 THEN 34000
        WHEN id = 21 THEN 49000
        WHEN id = 22 THEN 36000
        WHEN id = 23 THEN 58000
        WHEN id = 24 THEN 33000
        WHEN id = 25 THEN 51000
        WHEN id = 26 THEN 29000
    END,
    fuel_type = CASE 
        WHEN id IN (1,2,3,4,6,11,13,14,16,17,21,22,23,24,25,26) THEN 'Benzin'
        WHEN id IN (5,7,8,9,10,12,15,18,19,20) THEN 'Dizel'
    END,
    transmission = CASE 
        WHEN id IN (1,3,4,6,11,13,15,16,18,21,23,24,25,26) THEN 'Manuel'
        WHEN id IN (2,5,7,8,9,10,12,14,17,19,20,22) THEN 'Otomatik'
    END,
    segment = CASE 
        WHEN id IN (1,4,16,18,21,23,24,25,26) THEN 'Ekonomi'
        WHEN id IN (2,6,11,13,14,17,19,20,22) THEN 'Komfort'
        WHEN id IN (5,7,8,9,10,12) THEN 'Lüks'
        WHEN id IN (3,15) THEN 'SUV'
    END,
    color = CASE 
        WHEN id % 4 = 1 THEN 'Beyaz'
        WHEN id % 4 = 2 THEN 'Siyah'
        WHEN id % 4 = 3 THEN 'Gri'
        ELSE 'Gümüş'
    END,
    insurance_expiry_date = CURRENT_DATE + INTERVAL '6 months',
    insurance_company = CASE 
        WHEN id % 3 = 1 THEN 'Axa Sigorta'
        WHEN id % 3 = 2 THEN 'Allianz'
        ELSE 'HDI Sigorta'
    END;

-- Örnek bakım tarihleri (basit sabit tarihler)
UPDATE cars SET 
    last_maintenance_date = CURRENT_DATE - INTERVAL '2 months',
    next_maintenance_date = CURRENT_DATE + INTERVAL '4 months'
WHERE id IS NOT NULL;

-- Örnek araç özellikleri ekleme
INSERT INTO car_features (car_id, feature) VALUES
(1, 'ac'), (1, 'abs'), (1, 'airbag'),
(2, 'ac'), (2, 'abs'), (2, 'airbag'), (2, 'bluetooth'),
(3, 'ac'), (3, 'abs'), (3, 'airbag'), (3, 'gps'),
(4, 'ac'), (4, 'abs'),
(5, 'ac'), (5, 'abs'), (5, 'airbag'), (5, 'bluetooth'), (5, 'gps'), (5, 'sunroof'), (5, 'leather'),
(6, 'ac'), (6, 'abs'), (6, 'airbag'), (6, 'bluetooth'),
(7, 'ac'), (7, 'abs'), (7, 'airbag'), (7, 'bluetooth'), (7, 'gps'), (7, 'leather'),
(8, 'ac'), (8, 'abs'), (8, 'airbag'), (8, 'bluetooth'), (8, 'gps'), (8, 'sunroof'), (8, 'leather'),
(9, 'ac'), (9, 'abs'), (9, 'airbag'), (9, 'bluetooth'), (9, 'gps'), (9, 'leather'),
(10, 'ac'), (10, 'abs'), (10, 'airbag'), (10, 'bluetooth'), (10, 'gps'), (10, 'sunroof'), (10, 'leather'),
(11, 'ac'), (11, 'abs'), (11, 'airbag'), (11, 'bluetooth'), (11, 'gps'), (11, 'leather'),
(12, 'ac'), (12, 'abs'), (12, 'airbag'), (12, 'bluetooth'), (12, 'gps'), (12, 'sunroof'), (12, 'leather'),
(13, 'ac'), (13, 'abs'), (13, 'airbag'), (13, 'bluetooth'),
(14, 'ac'), (14, 'abs'), (14, 'airbag'), (14, 'bluetooth'), (14, 'gps'),
(15, 'ac'), (15, 'abs'), (15, 'airbag'),
(16, 'ac'), (16, 'abs'),
(17, 'ac'), (17, 'abs'), (17, 'airbag'),
(18, 'ac'), (18, 'abs'),
(19, 'ac'), (19, 'abs'), (19, 'airbag'), (19, 'bluetooth'),
(20, 'ac'), (20, 'abs'), (20, 'airbag'), (20, 'bluetooth'),
(21, 'ac'), (21, 'abs'),
(22, 'ac'), (22, 'abs'), (22, 'airbag'), (22, 'bluetooth'),
(23, 'ac'), (23, 'abs'),
(24, 'ac'), (24, 'abs'), (24, 'airbag'),
(25, 'ac'), (25, 'abs'),
(26, 'ac'), (26, 'abs'), (26, 'airbag');

-- Örnek bakım kayıtları
INSERT INTO maintenance_records (car_id, maintenance_date, maintenance_type, description, cost, mileage_at_service, service_provider) VALUES
(1, '2024-01-15', 'Periyodik', 'Genel bakım ve yağ değişimi', 850.00, 42000, 'Otoservis Merkez'),
(1, '2023-10-20', 'Fren', 'Fren balata ve disk değişimi', 1200.00, 38000, 'Fren Teknik'),
(2, '2024-02-01', 'Periyodik', 'Genel bakım ve filtreler', 950.00, 30000, 'Auto Plus'),
(3, '2023-12-10', 'Motor', 'Motor bakımı ve kontroller', 1500.00, 25000, 'Motor Servis'),
(4, '2024-01-05', 'Lastik', 'Lastik değişimi ve rot balans', 800.00, 52000, 'Lastik Center'),
(5, '2024-02-15', 'Periyodik', 'VIP araç özel bakım', 2500.00, 18000, 'Premium Servis'),
(15, '2023-11-30', 'Fren', 'Fren sistem bakımı', 1400.00, 55000, 'Fren Teknik'),
(15, '2023-09-15', 'Motor', 'Motor tam bakım', 2200.00, 50000, 'Motor Servis');

-- Araçların GPS konumlarını güncelleme (İstanbul çevresinde rastgele konumlar)
UPDATE cars SET 
    gps_latitude = CASE 
        WHEN id = 1 THEN 41.0082 + (RANDOM() - 0.5) * 0.1 -- İstanbul merkez çevresinde
        WHEN id = 2 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 3 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 4 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 5 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 6 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 7 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 8 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 9 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 10 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 11 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 12 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 13 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 14 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 15 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 16 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 17 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 18 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 19 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 20 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 21 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 22 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 23 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 24 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 25 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        WHEN id = 26 THEN 41.0082 + (RANDOM() - 0.5) * 0.1
        ELSE 41.0082
    END,
    gps_longitude = CASE 
        WHEN id = 1 THEN 28.9784 + (RANDOM() - 0.5) * 0.1 -- İstanbul merkez çevresinde
        WHEN id = 2 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 3 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 4 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 5 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 6 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 7 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 8 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 9 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 10 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 11 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 12 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 13 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 14 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 15 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 16 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 17 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 18 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 19 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 20 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 21 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 22 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 23 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 24 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 25 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        WHEN id = 26 THEN 28.9784 + (RANDOM() - 0.5) * 0.1
        ELSE 28.9784
    END,
    last_location_update = CURRENT_TIMESTAMP - (RANDOM() * INTERVAL '7 days')
WHERE id <= 26;

-- Örnek araç görselleri ekleme
INSERT INTO car_images (car_id, image_url) VALUES
(1, 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&h=600&fit=crop'),
(1, 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=600&fit=crop'),
(2, 'https://images.unsplash.com/photo-1549317336-206569e8475c?w=800&h=600&fit=crop'),
(3, 'https://images.unsplash.com/photo-1494905998402-395d579af36f?w=800&h=600&fit=crop'),
(3, 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop'),
(4, 'https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&h=600&fit=crop'),
(5, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&h=600&fit=crop'),
(5, 'https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=800&h=600&fit=crop'),
(6, 'https://images.unsplash.com/photo-1511919884226-fd3cad34687c?w=800&h=600&fit=crop'),
(7, 'https://images.unsplash.com/photo-1542362567-b07e54358753?w=800&h=600&fit=crop'),
(8, 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=800&h=600&fit=crop'),
(8, 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
(9, 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop'),
(10, 'https://images.unsplash.com/photo-1544265326-f6bed7c7b09c?w=800&h=600&fit=crop'),
(11, 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=800&h=600&fit=crop'),
(12, 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&h=600&fit=crop'),
(12, 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=600&fit=crop'),
(13, 'https://images.unsplash.com/photo-1549317336-206569e8475c?w=800&h=600&fit=crop'),
(14, 'https://images.unsplash.com/photo-1494905998402-395d579af36f?w=800&h=600&fit=crop'),
(15, 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop'),
(16, 'https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&h=600&fit=crop'),
(17, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&h=600&fit=crop'),
(18, 'https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=800&h=600&fit=crop'),
(19, 'https://images.unsplash.com/photo-1511919884226-fd3cad34687c?w=800&h=600&fit=crop'),
(20, 'https://images.unsplash.com/photo-1542362567-b07e54358753?w=800&h=600&fit=crop'),
(21, 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=800&h=600&fit=crop'),
(22, 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop'),
(23, 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop'),
(24, 'https://images.unsplash.com/photo-1544265326-f6bed7c7b09c?w=800&h=600&fit=crop'),
(25, 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=800&h=600&fit=crop'),
(26, 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&h=600&fit=crop');

-- GPS Location History örnek verileri (son 30 gün için)
INSERT INTO gps_location_history (car_id, latitude, longitude, recorded_at, address, speed, is_online, battery_level, source) VALUES
-- Araç 1 için GPS geçmişi
(1, 41.0082, 28.9784, CURRENT_TIMESTAMP - INTERVAL '1 day', 'Sultanahmet, İstanbul', 45.5, true, 85, 'GPS_DEVICE'),
(1, 41.0100, 28.9800, CURRENT_TIMESTAMP - INTERVAL '2 days', 'Beyazıt, İstanbul', 32.0, true, 87, 'GPS_DEVICE'),
(1, 41.0120, 28.9750, CURRENT_TIMESTAMP - INTERVAL '3 days', 'Kadıköy, İstanbul', 0.0, true, 90, 'GPS_DEVICE'),
(1, 41.0082, 28.9784, CURRENT_TIMESTAMP - INTERVAL '5 days', 'Sultanahmet, İstanbul', 28.5, true, 95, 'GPS_DEVICE'),

-- Araç 2 için GPS geçmişi
(2, 41.0150, 28.9850, CURRENT_TIMESTAMP - INTERVAL '2 hours', 'Beşiktaş, İstanbul', 65.0, true, 80, 'GPS_DEVICE'),
(2, 41.0200, 28.9900, CURRENT_TIMESTAMP - INTERVAL '1 day', 'Şişli, İstanbul', 55.5, true, 82, 'GPS_DEVICE'),
(2, 41.0180, 28.9950, CURRENT_TIMESTAMP - INTERVAL '3 days', 'Mecidiyeköy, İstanbul', 0.0, true, 88, 'GPS_DEVICE'),

-- Araç 3 için GPS geçmişi
(3, 41.0250, 28.9750, CURRENT_TIMESTAMP - INTERVAL '4 hours', 'Üsküdar, İstanbul', 42.0, true, 75, 'GPS_DEVICE'),
(3, 41.0300, 28.9700, CURRENT_TIMESTAMP - INTERVAL '1 day', 'Ümraniye, İstanbul', 38.5, true, 78, 'GPS_DEVICE'),
(3, 41.0280, 28.9650, CURRENT_TIMESTAMP - INTERVAL '4 days', 'Ataşehir, İstanbul', 0.0, true, 85, 'GPS_DEVICE'),

-- Araç 4 için GPS geçmişi
(4, 41.0050, 28.9500, CURRENT_TIMESTAMP - INTERVAL '6 hours', 'Bakırköy, İstanbul', 48.0, true, 92, 'GPS_DEVICE'),
(4, 41.0000, 28.9400, CURRENT_TIMESTAMP - INTERVAL '2 days', 'Florya, İstanbul', 35.0, true, 94, 'GPS_DEVICE'),

-- Araç 5 için GPS geçmişi
(5, 41.0800, 29.0000, CURRENT_TIMESTAMP - INTERVAL '3 hours', 'Sarıyer, İstanbul', 70.0, true, 88, 'GPS_DEVICE'),
(5, 41.0900, 29.0100, CURRENT_TIMESTAMP - INTERVAL '1 day', 'Zekeriyaköy, İstanbul', 45.0, true, 90, 'GPS_DEVICE'),
(5, 41.0850, 29.0050, CURRENT_TIMESTAMP - INTERVAL '5 days', 'Bahçeköy, İstanbul', 0.0, true, 95, 'GPS_DEVICE'),

-- Daha fazla araç için GPS geçmişi
(6, 41.0350, 28.9650, CURRENT_TIMESTAMP - INTERVAL '8 hours', 'Pendik, İstanbul', 52.0, true, 85, 'GPS_DEVICE'),
(7, 41.0450, 28.9550, CURRENT_TIMESTAMP - INTERVAL '12 hours', 'Kartal, İstanbul', 40.0, true, 87, 'GPS_DEVICE'),
(8, 41.0900, 28.0450, CURRENT_TIMESTAMP - INTERVAL '1 day', 'Beykoz, İstanbul', 35.0, true, 89, 'GPS_DEVICE'),
(9, 41.0200, 28.9200, CURRENT_TIMESTAMP - INTERVAL '2 days', 'Maltepe, İstanbul', 0.0, true, 92, 'GPS_DEVICE'),
(10, 41.0600, 28.9800, CURRENT_TIMESTAMP - INTERVAL '6 hours', 'Etiler, İstanbul', 60.0, true, 83, 'GPS_DEVICE');

-- ========================================
-- CUSTOMER PORTAL & NOTIFICATION SYSTEM
-- ========================================

-- Customers tablosuna user_id kolonu ekleme
ALTER TABLE customers ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- Foreign key constraint ekleme (eğer yoksa)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_customers_user_id' 
        AND table_name = 'customers'
    ) THEN
        ALTER TABLE customers ADD CONSTRAINT fk_customers_user_id FOREIGN KEY (user_id) REFERENCES users(id);
    END IF;
END $$;

-- Reservations tablosu
CREATE TABLE IF NOT EXISTS reservations (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    "end" DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10,2),
    special_requests VARCHAR(500),
    note VARCHAR(500),
    confirmed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    created_by VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_date TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE
);

-- Reservation ratings tablosu
CREATE TABLE IF NOT EXISTS reservation_ratings (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment VARCHAR(1000),
    car_rating BOOLEAN DEFAULT false,
    is_public BOOLEAN DEFAULT true,
    created_by VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_date TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Notifications tablosu
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    channel VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP,
    read_at TIMESTAMP,
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    created_by VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_date TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Index'ler
CREATE INDEX IF NOT EXISTS idx_reservations_customer_id ON reservations(customer_id);
CREATE INDEX IF NOT EXISTS idx_reservations_car_id ON reservations(car_id);
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(status);
CREATE INDEX IF NOT EXISTS idx_reservations_dates ON reservations(start_date, "end");

CREATE INDEX IF NOT EXISTS idx_reservation_ratings_customer_id ON reservation_ratings(customer_id);
CREATE INDEX IF NOT EXISTS idx_reservation_ratings_reservation_id ON reservation_ratings(reservation_id);

CREATE INDEX IF NOT EXISTS idx_notifications_customer_id ON notifications(customer_id);
CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type);

-- Customers tablosuna user bağlantısı için örnek veri (mevcut customers için)
-- İlk customer'ı admin user ile bağlayalım
UPDATE customers SET user_id = (SELECT id FROM users WHERE username = 'admin' LIMIT 1) WHERE id = 1;
