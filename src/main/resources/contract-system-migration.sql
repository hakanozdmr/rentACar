-- Contract System Migration Script
-- Bu script sözleşme yönetimi ve araç durum kontrolü için gerekli tabloları oluşturur

-- Contract Templates tablosu
CREATE TABLE IF NOT EXISTS contract_templates (
    id BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_date TIMESTAMP,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    template_key VARCHAR(50) NOT NULL UNIQUE,
    content TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_default BOOLEAN NOT NULL DEFAULT false,
    version INTEGER,
    variables TEXT,
    last_used_at TIMESTAMP,
    usage_count BIGINT
);

-- Contracts tablosu
CREATE TABLE IF NOT EXISTS contracts (
    id BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_date TIMESTAMP,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    rental_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    template_id BIGINT,
    contract_number VARCHAR(255) NOT NULL UNIQUE,
    signed_date DATE NOT NULL,
    signed_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    terms TEXT,
    conditions TEXT,
    customer_signature VARCHAR(500),
    company_signature VARCHAR(500),
    signed_by VARCHAR(100),
    e_signature_verified_at TIMESTAMP,
    e_signature_hash VARCHAR(500),
    pdf_path VARCHAR(500),
    witness_name VARCHAR(100),
    expiry_date DATE,
    notes VARCHAR(1000),
    
    FOREIGN KEY (rental_id) REFERENCES rentals(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (template_id) REFERENCES contract_templates(id) ON DELETE SET NULL
);

-- Rental Documents tablosu
CREATE TABLE IF NOT EXISTS rental_documents (
    id BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_date TIMESTAMP,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    rental_id BIGINT NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    uploaded_at TIMESTAMP,
    uploaded_by VARCHAR(100),
    thumbnail_path VARCHAR(500),
    is_verified BOOLEAN NOT NULL DEFAULT false,
    verified_at TIMESTAMP,
    verified_by VARCHAR(100),
    metadata VARCHAR(1000),
    
    FOREIGN KEY (rental_id) REFERENCES rentals(id) ON DELETE CASCADE
);

-- Vehicle Condition Checks tablosu
CREATE TABLE IF NOT EXISTS vehicle_condition_checks (
    id BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_date TIMESTAMP,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    rental_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    check_type VARCHAR(50) NOT NULL,
    mileage_at_check BIGINT NOT NULL,
    fuel_level INTEGER NOT NULL,
    body_has_damage BOOLEAN NOT NULL DEFAULT false,
    body_damage_description TEXT,
    interior_has_damage BOOLEAN NOT NULL DEFAULT false,
    interior_damage_description TEXT,
    windows_have_damage BOOLEAN NOT NULL DEFAULT false,
    windows_damage_description VARCHAR(500),
    tires_have_damage BOOLEAN NOT NULL DEFAULT false,
    tires_damage_description VARCHAR(500),
    has_scratches BOOLEAN NOT NULL DEFAULT false,
    scratches_description TEXT,
    damage_cost DECIMAL(10,2),
    performed_by VARCHAR(100) NOT NULL,
    performed_at TIMESTAMP NOT NULL,
    customer_note VARCHAR(500),
    staff_note VARCHAR(500),
    is_confirmed BOOLEAN NOT NULL DEFAULT false,
    confirmed_at TIMESTAMP,
    needs_maintenance BOOLEAN NOT NULL DEFAULT false,
    maintenance_note TEXT,
    
    FOREIGN KEY (rental_id) REFERENCES rentals(id) ON DELETE CASCADE,
    FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE
);

-- Index'ler (performans için)
CREATE INDEX IF NOT EXISTS idx_contracts_rental_id ON contracts(rental_id);
CREATE INDEX IF NOT EXISTS idx_contracts_customer_id ON contracts(customer_id);
CREATE INDEX IF NOT EXISTS idx_contracts_status ON contracts(status);
CREATE INDEX IF NOT EXISTS idx_contracts_signed_date ON contracts(signed_date);
CREATE INDEX IF NOT EXISTS idx_contracts_contract_number ON contracts(contract_number);

CREATE INDEX IF NOT EXISTS idx_contract_templates_template_key ON contract_templates(template_key);
CREATE INDEX IF NOT EXISTS idx_contract_templates_active ON contract_templates(is_active);

CREATE INDEX IF NOT EXISTS idx_rental_documents_rental_id ON rental_documents(rental_id);
CREATE INDEX IF NOT EXISTS idx_rental_documents_type ON rental_documents(document_type);
CREATE INDEX IF NOT EXISTS idx_rental_documents_uploaded_at ON rental_documents(uploaded_at);

CREATE INDEX IF NOT EXISTS idx_vehicle_condition_checks_rental_id ON vehicle_condition_checks(rental_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_condition_checks_car_id ON vehicle_condition_checks(car_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_condition_checks_type ON vehicle_condition_checks(check_type);
CREATE INDEX IF NOT EXISTS idx_vehicle_condition_checks_performed_at ON vehicle_condition_checks(performed_at);

-- Örnek şablon verileri
INSERT INTO contract_templates (name, description, template_key, content, is_active, is_default) 
VALUES (
    'Standart Kiralama Sözleşmesi',
    'Tüm araç tipleri için standart kiralama sözleşmesi',
    'STANDARD_RENTAL',
    E'# ARAÇ KİRALAMA SÖZLEŞMESİ\n\n**Sözleşme No:** {contractNumber}\n**Tarih:** {signedDate}\n\n## Taraflar\n\n### Kiralayan:\n- Firma: RentACar\n- Yetkili: Şirket Yetkilisi\n\n### Kiracı:\n- Ad Soyad: {customerName}\n- TC Kimlik No: {customerIdNumber}\n- Ehliyet No: {driverLicenseNumber}\n- Adres: {customerAddress}\n\n## Araç Bilgileri\n- Marka: {carBrand}\n- Model: {carModel}\n- Plaka: {carPlate}\n- Model Yılı: {modelYear}\n\n## Kiralama Detayları\n- Başlangıç Tarihi: {startDate}\n- Bitiş Tarihi: {endDate}\n- Toplam Tutar: {totalAmount} TL\n\n## Şartlar ve Koşullar\n\n1. Kiracı, aracı teslim aldığı anda kontrol etmiş ve herhangi bir hasar/eksiklik görmemiş ise kabul edilmiş sayılır.\n2. Araç, sadece kiracı tarafından kullanılabilir. Başkasına devredilemez.\n3. Araç teslim edilirken bulunduğu yakıt seviyesi ile teslim alınmalıdır.\n4. Araçta herhangi bir değişiklik yapılamaz.\n5. Trafik cezaları kiracıya aittir.\n6. Sözleşme süresince oluşacak her türlü hasar kiracıya aittir.\n\n## İmzalar\n\nKiracı:\n____________________\n{m signedBy}\n\nKiralayan:\n____________________\nRentACar Yetkilisi',
    true,
    true
);

-- Yorumlar (dokümantasyon için)
COMMENT ON TABLE contract_templates IS 'Sözleşme şablonları';
COMMENT ON TABLE contracts IS 'Kiralama sözleşmeleri';
COMMENT ON TABLE rental_documents IS 'Kiralama ile ilgili belgeler ve fotoğraflar';
COMMENT ON TABLE vehicle_condition_checks IS 'Araç durum kontrolü (teslim/teslim alma)';

COMMENT ON COLUMN contract_templates.template_key IS 'Şablon anahtarı (örn: STANDARD_RENTAL, LUXURY_RENTAL)';
COMMENT ON COLUMN contract_templates.content IS 'Şablon içeriği HTML veya markdown formatında';
COMMENT ON COLUMN contract_templates.variables IS 'Kullanılabilir değişkenler JSON formatında';

COMMENT ON COLUMN contracts.status IS 'Sözleşme durumu: DRAFT, PENDING_SIGNATURE, SIGNED, EXPIRED, CANCELLED, VERIFIED';
COMMENT ON COLUMN contracts.customer_signature IS 'Müşteri imzası (base64 veya dosya yolu)';
COMMENT ON COLUMN contracts.e_signature_hash IS 'E-imza hash değeri (güvenlik)';

COMMENT ON COLUMN rental_documents.document_type IS 'Belge tipi: DELIVERY_PHOTO, PICKUP_PHOTO, DAMAGE_REPORT, CONTRACT, etc.';

COMMENT ON COLUMN vehicle_condition_checks.check_type IS 'Kontrol tipi: TESLIM, TESLIM_ALMA';
COMMENT ON COLUMN vehicle_condition_checks.fuel_level IS 'Yakıt seviyesi (0-100)';
COMMENT ON COLUMN vehicle_condition_checks.needs_maintenance IS 'Araç bakıma ihtiyaç duyuyor mu';


