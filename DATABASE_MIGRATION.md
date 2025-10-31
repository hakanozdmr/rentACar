# Database Migration: Rentals ve Reservations Tablolarının Birleştirilmesi

## Değişiklik Özeti
`rentals` tablosuna `reservations` tablosundan gelen yeni alanlar eklendi. Bu değişiklik ile artık rental ve reservation verileri tek tabloda tutuluyor.

## Yeni Eklenen Alanlar

### rentals tablosuna eklenen kolonlar:
- `status` VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED' - Rental durumu (PENDING, CONFIRMED, CANCELLED, COMPLETED, EXPIRED)
- `total_amount` DECIMAL(10,2) - Toplam kiralama tutarı
- `special_requests` VARCHAR(500) - Müşterinin özel istekleri
- `confirmed_at` TIMESTAMP - Onaylanma tarihi
- `cancelled_at` TIMESTAMP - İptal tarihi (varsa)

## Migration Adımları

### 1. Otomatik Migration (Hibernate DDL)
Eğer `spring.jpa.hibernate.ddl-auto = update` ayarı aktifse, Hibernate otomatik olarak yeni kolonları ekleyecektir.

### 2. Manuel Migration
Daha kontrollü bir yaklaşım için `src/main/resources/db-migration.sql` dosyasını çalıştırın:

```sql
-- PostgreSQL için migration script'i
psql -h localhost -U postgres -d rentACar -f src/main/resources/db-migration.sql
```

### 3. Migration Script İçeriği
Migration script'i şunları yapar:
- Yeni kolonları ekler (IF NOT EXISTS ile güvenli)
- Mevcut kayıtları default değerlerle günceller
- Performance için index'ler ekler
- Kolonlara açıklama ekler

## Veri Güncellemeleri

### Mevcut Rental Kayıtları
- Tüm mevcut rental kayıtları `status = 'CONFIRMED'` olarak güncellenir
- `total_amount` değeri otomatik hesaplanır (günlük fiyat * gün sayısı)
- `confirmed_at` değeri `created_date` ile aynı yapılabilir

### Yeni Kayıtlar
- Yeni rental kayıtları oluşturulurken tüm alanlar doldurulur
- `confirmReservation` metodunda reservation verileri rental'a kopyalanır

## Backward Compatibility
Bu değişiklik backward compatible'dır:
- Mevcut kodlar çalışmaya devam eder
- Yeni alanlar nullable/optional olarak eklenmiştir
- Frontend'de fallback mekanizmaları mevcuttur

## Test Edilmesi Gerekenler
1. Mevcut rental kayıtlarının doğru şekilde görüntülenmesi
2. Yeni rental oluşturma işlemi
3. Status bazlı filtreleme ve arama
4. Frontend'de status gösterimi
5. Değerlendirme sistemi çalışması

## Rollback (Gerekirse)
Eğer geri alınması gerekirse:
```sql
ALTER TABLE rentals 
DROP COLUMN IF EXISTS status,
DROP COLUMN IF EXISTS total_amount,
DROP COLUMN IF EXISTS special_requests,
DROP COLUMN IF EXISTS confirmed_at,
DROP COLUMN IF EXISTS cancelled_at;

DROP INDEX IF EXISTS idx_rentals_status;
DROP INDEX IF EXISTS idx_rentals_confirmed_at;
```


