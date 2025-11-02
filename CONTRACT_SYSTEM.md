# Sözleşme Yönetimi ve Teslim/Teslim Alma Sistemi

Bu dokümantasyon, RentACar projesine eklenen sözleşme yönetimi ve araç durum kontrolü sisteminin kullanımını açıklar.

## 📋 İçindekiler

- [Genel Bakış](#genel-bakış)
- [Özellikler](#özellikler)
- [Kurulum](#kurulum)
- [API Kullanımı](#api-kullanımı)
- [Veritabanı Yapısı](#veritabanı-yapısı)

## 🎯 Genel Bakış

Sistem aşağıdaki ana bileşenlerden oluşur:

1. **Sözleşme Yönetimi**: Otomatik sözleşme oluşturma, imza yönetimi, şablonlar
2. **Belge Yönetimi**: Araç fotoğrafları, hasar raporları, diğer belgeler
3. **Araç Durum Kontrolü**: Teslim ve teslim alma sırasında araç durumunu kaydetme
4. **Sözleşme Şablonları**: Yeniden kullanılabilir sözleşme şablonları

## ✨ Özellikler

### Sözleşme Yönetimi
- ✅ Otomatik sözleşme numarası oluşturma
- ✅ Sözleşme şablonları ile hızlı oluşturma
- ✅ İmza yönetimi (müşteri ve şirket)
- ✅ E-imza desteği (hash verification)
- ✅ PDF oluşturma desteği
- ✅ Sözleşme durum takibi (Taslak, İmza Bekliyor, İmzalandı, vb.)

### Belge Yönetimi
- ✅ Çoklu belge tipleri (fotoğraflar, raporlar, kimlikler, vb.)
- ✅ Dosya yükleme ve saklama
- ✅ Belge doğrulama sistemi
- ✅ Thumbnail desteği
- ✅ Metadata saklama

### Araç Durum Kontrolü
- ✅ Teslim kontrolü
- ✅ Teslim alma kontrolü
- ✅ Hasar tespiti (gövde, iç mekan, cam, lastik, çizik)
- ✅ Yakıt seviyesi kaydı
- ✅ Kilometre takibi
- ✅ Müşteri onay sistemi
- ✅ Otomatik hasar maliyeti hesaplama

### Şablon Yönetimi
- ✅ Değişken bazlı şablonlar
- ✅ Varsayılan şablon desteği
- ✅ Aktif/pasif yönetimi
- ✅ Kullanım istatistikleri

## 🚀 Kurulum

### 1. Veritabanı Migration

PostgreSQL veritabanınıza migration script'ini çalıştırın:

```bash
psql -h localhost -U postgres -d rentACar -f src/main/resources/contract-system-migration.sql
```

Bu script şu tabloları oluşturur:
- `contracts` - Sözleşmeler
- `contract_templates` - Sözleşme şablonları
- `rental_documents` - Kiralama belgeleri
- `vehicle_condition_checks` - Araç durum kontrolleri

### 2. Spring Boot Uygulamasını Başlatın

```bash
mvn spring-boot:run
```

### 3. Swagger UI ile Test Edin

Tarayıcınızda şu adrese gidin:
```
http://localhost:8080/swagger-ui.html
```

## 📡 API Kullanımı

### Sözleşmeler

#### Tüm Sözleşmeleri Getir
```http
GET /api/contracts
```

#### Sözleşme Oluştur
```http
POST /api/contracts
Content-Type: application/json

{
  "rentalId": 1,
  "customerId": 1,
  "templateId": 1,
  "contractNumber": "KIR-2024-001",
  "signedDate": "2024-01-15",
  "terms": "Şartlar ve koşullar...",
  "conditions": "Özel koşullar..."
}
```

#### Sözleşmeyi İmzala
```http
POST /api/contracts/{id}/sign?customerSignature=base64...&companySignature=base64...
```

#### E-İmzayı Doğrula
```http
POST /api/contracts/{id}/verify?eSignatureHash=abc123...
```

### Sözleşme Şablonları

#### Tüm Şablonları Getir
```http
GET /api/contract-templates
```

#### Varsayılan Şablonu Getir
```http
GET /api/contract-templates/default
```

#### Şablon Oluştur
```http
POST /api/contract-templates
Content-Type: application/json

{
  "name": "Standart Kiralama",
  "description": "Standart kiralama sözleşmesi",
  "templateKey": "STANDARD_RENTAL",
  "content": "# ARAÇ KİRALAMA SÖZLEŞMESİ\n\n**Sözleşme No:** {contractNumber}\n...",
  "isActive": true,
  "isDefault": false,
  "variables": "{\"contractNumber\": \"Sözleşme Numarası\", ...}"
}
```

### Belgeler

#### Belge Yükle
```http
POST /api/rental-documents/upload
Content-Type: multipart/form-data

file: [binary data]
```

#### Kiralama Belgelerini Getir
```http
GET /api/rental-documents/rental/{rentalId}
```

#### Belge Doğrula
```http
POST /api/rental-documents/{id}/verify?verifiedBy=admin
```

### Araç Durum Kontrolü

#### Teslim Kontrolü Oluştur
```http
POST /api/vehicle-condition-checks
Content-Type: application/json

{
  "rentalId": 1,
  "carId": 1,
  "checkType": "TESLIM",
  "mileageAtCheck": 15000,
  "fuelLevel": 80,
  "bodyHasDamage": false,
  "interiorHasDamage": false,
  "windowsHaveDamage": false,
  "tiresHaveDamage": false,
  "hasScratches": false,
  "performedBy": "Muhammet Yılmaz",
  "performedAt": "2024-01-15T10:00:00"
}
```

#### Teslim Alma Kontrolü
```http
POST /api/vehicle-condition-checks
Content-Type: application/json

{
  "rentalId": 1,
  "carId": 1,
  "checkType": "TESLIM_ALMA",
  "mileageAtCheck": 15320,
  "fuelLevel": 60,
  "bodyHasDamage": true,
  "bodyDamageDescription": "Ön tamponda çizik",
  "performedBy": "Ayşe Demir",
  "performedAt": "2024-01-20T14:00:00"
}
```

#### Kontrolleri Karşılaştır
```http
GET /api/vehicle-condition-checks/rental/{rentalId}/compare
```

Bu endpoint, teslim ve teslim alma kontrollerini karşılaştırarak farkları tespit eder:
- Yakıt farkı hesaplama
- Yeni hasarları tespit etme
- Hasar maliyeti önermesi

## 🗃️ Veritabanı Yapısı

### contracts Tablosu
- `id` - Sözleşme ID
- `rental_id` - Kiralama ID (foreign key)
- `customer_id` - Müşteri ID (foreign key)
- `template_id` - Şablon ID (foreign key)
- `contract_number` - Sözleşme numarası (unique)
- `signed_date` - İmza tarihi
- `status` - Sözleşme durumu (DRAFT, PENDING_SIGNATURE, SIGNED, EXPIRED, CANCELLED, VERIFIED)
- `customer_signature` - Müşteri imzası
- `company_signature` - Şirket imzası
- `e_signature_hash` - E-imza hash
- `pdf_path` - PDF dosya yolu

### contract_templates Tablosu
- `id` - Şablon ID
- `name` - Şablon adı
- `template_key` - Şablon anahtarı (unique)
- `content` - Şablon içeriği (HTML/Markdown)
- `is_active` - Aktif mi?
- `is_default` - Varsayılan mı?
- `variables` - Değişken listesi (JSON)

### rental_documents Tablosu
- `id` - Belge ID
- `rental_id` - Kiralama ID (foreign key)
- `file_name` - Dosya adı
- `file_type` - Dosya tipi
- `file_size` - Dosya boyutu
- `file_path` - Dosya yolu
- `document_type` - Belge tipi (DELIVERY_PHOTO, PICKUP_PHOTO, DAMAGE_REPORT, vb.)
- `is_verified` - Doğrulandı mı?

### vehicle_condition_checks Tablosu
- `id` - Kontrol ID
- `rental_id` - Kiralama ID (foreign key)
- `car_id` - Araç ID (foreign key)
- `check_type` - Kontrol tipi (TESLIM, TESLIM_ALMA)
- `mileage_at_check` - Kontrol anındaki kilometre
- `fuel_level` - Yakıt seviyesi (0-100)
- `body_has_damage` - Gövde hasarı var mı?
- `body_damage_description` - Gövde hasarı açıklaması
- `interior_has_damage` - İç mekan hasarı var mı?
- `windows_have_damage` - Cam hasarı var mı?
- `tires_have_damage` - Lastik hasarı var mı?
- `has_scratches` - Çizik var mı?
- `damage_cost` - Hasar maliyeti
- `performed_by` - Kontrolü yapan
- `performed_at` - Kontrol zamanı
- `is_confirmed` - Müşteri onayladı mı?

## 🔒 Güvenlik

Tüm API endpoint'leri JWT authentication gerektirir. Request header'ında token göndermelisiniz:

```http
Authorization: Bearer <your-jwt-token>
```

## 📝 Notlar

- Sözleşme numaraları otomatik oluşturulur (format: KIR-YYYY-MMDDNNN)
- Belge dosyaları `uploads/documents/` klasöründe saklanır
- E-imza hash'leri SHA-256 algoritması ile oluşturulmalıdır
- Şablon içeriğinde `{variableName}` formatında değişkenler kullanılabilir
- Araç durum kontrolleri karşılaştırıldığında otomatik olarak hasar farkları tespit edilir

## 🐛 Sorun Giderme

### Migration Hatası
```bash
# Tablolar zaten varsa hata alabilirsiniz
# Script IF NOT EXISTS kullanır, güvenli
```

### Dosya Yükleme Hatası
```bash
# uploads/documents/ klasörünün yazma izni olmalı
mkdir -p uploads/documents
chmod 755 uploads/documents
```

### ModelMapper Hatası
```bash
# Eğer mapping hatası alırsanız, ModelMapperBean.java dosyasına yeni mapping ekleyin
```

## 📚 İleri Seviye Özellikler (Gelecek)

- [ ] PDF oluşturma entegrasyonu (iText veya Apache PDFBox)
- [ ] E-imza servisi entegrasyonu (SMS Onay, eDevlet, vb.)
- [ ] Email ile otomatik sözleşme gönderme
- [ ] OCR ile hasar tespiti
- [ ] Fotoğraf optimizasyonu ve sıkıştırma
- [ ] Cloud storage entegrasyonu (AWS S3, Azure Blob, vb.)
- [ ] İstatistik ve raporlama dashboard'u
- [ ] Mobil uygulama desteği

## 📄 Lisans

Bu sistem RentACar projesinin bir parçasıdır.


