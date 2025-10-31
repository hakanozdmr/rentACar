# RentACar Frontend

Araç kiralama yönetim sistemi için React tabanlı modern web arayüzü.

## Özellikler

- 📊 **Dashboard**: Sistem genelinde istatistikler ve hızlı erişim
- 🚗 **Araç Yönetimi**: Araçların CRUD işlemleri, durum filtreleme
- 👥 **Müşteri Yönetimi**: Müşteri bilgileri ve arama özellikleri
- 📋 **Kiralama Yönetimi**: Rezervasyon işlemleri ve tarih planlaması
- 🏷️ **Marka/Model Yönetimi**: Araç kategorilerinin yönetimi

## Teknolojiler

- **React 18** - Modern React hooks ve functional components
- **TypeScript** - Tip güvenliği ve geliştirici deneyimi
- **Material-UI (MUI)** - Modern ve responsive UI komponenleri
- **React Query** - Server state yönetimi ve caching
- **React Hook Form** - Form yönetimi ve validasyon
- **React Router** - Sayfa yönlendirme
- **Axios** - HTTP istekleri
- **Day.js** - Tarih işlemleri

## Kurulum

1. Bağımlılıkları yükleyin:
```bash
npm install
```

2. Geliştirme sunucusunu başlatın:
```bash
npm start
```

3. Tarayıcıda `http://localhost:3000` adresine gidin.

## Backend Entegrasyonu

Frontend, `http://localhost:8080/api` adresindeki Spring Boot backend'i ile entegre çalışır.

## Google Maps Entegrasyonu

GPS konum görüntüleme özelliği için Google Maps API anahtarı gereklidir:

1. [Google Cloud Console](https://console.developers.google.com/apis/credentials) adresinden API anahtarı oluşturun
2. **Maps JavaScript API**'yi etkinleştirin
3. Frontend klasöründe `.env` dosyası oluşturun:
   ```
   REACT_APP_GOOGLE_MAPS_API_KEY=your_api_key_here
   ```
4. Uygulamayı yeniden başlatın

**Not**: API anahtarı olmadan GPS konumları görüntülenemez, ancak uygulama çalışmaya devam eder.

## Yapı

```
src/
├── components/          # Yeniden kullanılabilir komponenler
│   ├── Layout.tsx      # Ana layout ve navigasyon
│   └── GoogleMap.tsx   # Google Maps entegrasyonu
├── pages/              # Sayfa komponenleri
│   ├── Dashboard.tsx   # Ana sayfa
│   ├── BrandsPage.tsx  # Marka yönetimi
│   ├── ModelsPage.tsx  # Model yönetimi
│   ├── CarsPage.tsx    # Araç yönetimi
│   ├── CustomersPage.tsx # Müşteri yönetimi
│   └── RentalsPage.tsx # Kiralama yönetimi
├── services/           # API servisleri
│   └── api.ts         # Backend API entegrasyonu
└── App.tsx            # Ana uygulama komponenti
```

## Özellik Detayları

### Dashboard
- Sistem istatistikleri (toplam araç, müşteri, aktif kiralama)
- Araç durum dağılımı
- Hızlı işlem butonları

### Araç Yönetimi
- Araç ekleme/düzenleme/silme
- Durum filtreleme (Müsait, Kiralandı, Bakımda)
- Model ve marka bilgileri ile arama
- GPS konum görüntüleme (Google Maps entegrasyonu)

### Müşteri Yönetimi
- Detaylı müşteri bilgileri
- TC Kimlik ve Ehliyet numarası kontrolü
- Ad, soyad ve email ile arama

### Kiralama Yönetimi
- Tarih bazlı rezervasyon
- Otomatik fiyat hesaplama
- Çakışma kontrolü
- Detaylı kiralama bilgileri

## Responsive Tasarım

Uygulama mobil ve desktop cihazlarda optimize edilmiştir:
- Material-UI Grid sistemi
- Responsive navigasyon
- Mobil uyumlu formlar


