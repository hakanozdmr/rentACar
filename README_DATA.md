# RentACar Projesi - Örnek Veriler

Bu proje için oluşturulmuş örnek veriler aşağıdaki dosyalarda bulunmaktadır:

## 🗃️ Veritabanı Dosyaları

### 1. `database/sample_data.sql`
**Manuel SQL import için** - PostgreSQL veritabanınıza manuel olarak yükleyeceğiniz örnek veriler.

**Kullanım:**
```bash
psql -h localhost -U postgres -d rentACar -f database/sample_data.sql
```

### 2. `src/main/resources/data.sql`
**Otomatik yükleme** - Spring Boot uygulaması başladığında otomatik olarak veritabanına yüklenir.

**Özellikler:**
- `spring.sql.init.mode=always` - Her başlatmada çalışır
- `spring.sql.init.continue-on-error=true` - Hata durumunda devam eder

## 🎯 Frontend Test Verileri

### 3. `frontend/src/data/sampleData.ts`
Frontend geliştirme ve test için kullanılabilecek TypeScript veri dosyası.

**İçerik:**
- Örnek markalar, modeller, araçlar
- Müşteri verileri
- Kiralama kayıtları
- Yardımcı fonksiyonlar

## 📊 Veri İçeriği

### Markalar (10 adet)
Toyota, Ford, BMW, Mercedes-Benz, Audi, Volkswagen, Honda, Hyundai, Renault, Peugeot

### Modeller (26 adet)
Her marka için 2-3 model (Corolla, Camry, Focus, Mustang, 3 Series, C-Class vb.)

### Araçlar (26 adet)
- **Farklı plakalar**: 34ABC123, 06DEF456, 35GHI789...
- **Günlük fiyatlar**: 350-1200 TL arası
- **Model yılları**: 2022-2023
- **Durumlar**: 1-Müsait, 2-Kiralandı, 3-Bakımda

### Müşteriler (10 adet)
- **Gerçekçi Türkçe isimler**: Ahmet Yılmaz, Ayşe Kaya, Mehmet Demir...
- **Türkiye şehirleri**: İstanbul, İzmir, Bursa, Konya, Antalya...
- **TC Kimlik ve Ehliyet numaraları**

### Kiralamalar (10 adet)
- **Geçmiş ve aktif kiralamalar**
- **Farklı araç-müşteri kombinasyonları**
- **Ek maliyetler ve notlar**

## 🚀 Kurulum Adımları

1. **Backend veritabanı**:
   ```bash
   # PostgreSQL veritabanını oluşturun
   createdb rentACar
   
   # Spring Boot uygulamasını başlatın (otomatik veri yükler)
   mvn spring-boot:run
   ```

2. **Manuel SQL import** (opsiyonel):
   ```bash
   psql -h localhost -U postgres -d rentACar -f database/sample_data.sql
   ```

3. **Frontend test**:
   ```typescript
   import { sampleBrands, sampleCars } from './data/sampleData';
   ```

## ⚠️ Notlar

- **TC Kimlik numaraları** sadece örnek formatındadır
- **Telefon numaraları** gerçek değildir
- **Tarihler** 2024 yılı için günceldir
- **Fiyatlar** TL cinsinden ve gerçekçi aralıklardadır

## 🔄 Veri Güncelleme

Yeni örnek veriler eklemek için:
1. SQL dosyalarını düzenleyin
2. Frontend TypeScript dosyasını güncelleyin
3. Uygulamayı yeniden başlatın


