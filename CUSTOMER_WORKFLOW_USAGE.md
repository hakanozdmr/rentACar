# Müşteri Kiralama Workflow Kullanım Kılavuzu

## Genel Bakış
Müşteriler artık portal üzerinden adım adım kiralama işlemi oluşturabilirler. Bu workflow, araç seçiminden ödeme işlemine kadar tüm süreci yönetir.

## Erişim
1. Müşteri girişi yapın
2. Sol menüden "Müşteri Portalı" sekmesine tıklayın
3. Portal içinde "Yeni Kiralama" sekmesine tıklayın

## Akış Adımları

### 1️⃣ Araç ve Tarih Seçimi
- **Başlangıç Tarihi**: Geçmiş tarih seçilemez
- **Bitiş Tarihi**: Başlangıç tarihinden sonra olmalı
- **Araç Listesi**: Seçilen tarih aralığında uygun araçlar gösterilir
- **Araç Seçimi**: Tabloda araç üzerine tıklayarak seçin
- Seçim yapıldığında araç vurgulanır ve ✓ işareti görünür

**Not**: Seçilen tarih aralığında uygun araç yoksa bilgilendirme mesajı gösterilir.

### 2️⃣ Sözleşme Şablonu
- Aktif sözleşme şablonları listelenir
- Her şablon kart olarak gösterilir
- Şablon üzerine tıklayarak seçin
- Seçilen şablon vurgulanır ve ✓ işareti görünür

### 3️⃣ Sözleşme Özeti
Bu adımda seçilen bilgilerin özeti gösterilir:
- **Araç**: Marka, model, plaka
- **Kiralama Süresi**: Gün sayısı
- **Toplam Tutar**: Otomatik hesaplanır
- **Yakıt Bilgisi**: "Dolu teslim alınacak"

**Not**: Bu adım sadece bilgilendirme amaçlıdır, backend işlemi yapılmaz.

### 4️⃣ Ödeme Bilgileri
- Bilgilendirme mesajı gösterilir
- "Kiralama Oluştur ve Öde" butonuna tıklandığında backend işlemleri başlar
- İşlem sırası:
  1. Kiralama kaydı oluşturulur
  2. Sözleşme oluşturulur
  3. Sözleşme otomatik imzalanır
  4. Ödeme kaydı oluşturulur
  5. Başarı mesajı gösterilir

### 5️⃣ Tamamlandı
- ✅ Onay ikonu gösterilir
- Kiralama numarası gösterilir
- "Portala Dön" butonuyla müşteri portalına dönülebilir

## Backend İşlem Akışı

```
1. createRentalMutation
   ↓ (onSuccess)
2. createContractMutation
   ↓ (onSuccess)
3. signContractMutation
   ↓ (onSuccess)
4. createPaymentMutation
   ↓ (onSuccess)
5. ✅ Workflow tamamlandı
```

## Özellikler
- ✅ Adım adım gösterim (Stepper component)
- ✅ Geri butonu ile adımlar arası geri dönüş
- ✅ Form validasyonu (eksik alan kontrolü)
- ✅ Loading state gösterimi (her adımda)
- ✅ Hata mesajları (her adımda)
- ✅ Başarı mesajları
- ✅ Otomatik portal yenileme (query invalidation)
- ✅ Stale closure önleme (useRef kullanımı)

## Kullanım Senaryoları

### Senaryo 1: Başarılı Kiralama
1. Müşteri araç ve tarih seçer
2. Sözleşme şablonu seçer
3. Özeti inceler
4. Ödeme butonuna tıklar
5. İşlem tamamlanır
6. Portal'a döner

### Senaryo 2: Hata Durumu
1. Müşteri araç seçmeden ilerlemeye çalışır
2. "Lütfen araç ve tarih seçin" mesajı gösterilir
3. Müşteri araç seçer ve devam eder

### Senaryo 3: Tarih Hatası
1. Müşteri geçmiş bir tarih seçmeye çalışır
2. Geçmiş tarihler devre dışıdır
3. Müşteri gelecek bir tarih seçer

## Teknik Detaylar

### Kullanılan Teknolojiler
- **React**: UI kütüphanesi
- **Material-UI**: UI bileşenleri (Stepper, Cards, Tables)
- **React Query**: Server state yönetimi
- **Axios**: HTTP istekleri
- **Day.js**: Tarih işlemleri

### State Yönetimi
- `useState`: Form ve UI state'leri
- `useRef`: Stale closure önleme
- `useQuery`: Server data çekme
- `useMutation`: Server data gönderme

### Validasyon
- Client-side validasyon: Eksik alan kontrolü
- Server-side validasyon: Backend'den gelen hata mesajları
- Date validasyon: Min/Max tarih kontrolü

### Loading States
- Araç listesi yüklenirken: CircularProgress
- İşlemler yapılırken: Buton içinde küçük CircularProgress
- İşlem sonrası: Portal yenileme

## Geliştirici Notları

### Console Logs
Development ortamında her mutation'ın başarı/başarısızlık durumu console'a yazılır:
- Rental created
- Rental ID
- Creating contract with
- Contract created
- Contract signed
- Payment completed

### Hata Mesajları
Hata durumunda snackbar ile kullanıcıya gösterilir:
- Kiralama oluşturulurken hata
- Sözleşme oluşturulurken hata
- Sözleşme imzalanırken hata
- Ödeme kaydedilirken hata

### Query Invalidation
İşlem tamamlandığında `myRentals` query'si invalidate edilir, böylece müşteri portalındaki kiralama listesi otomatik güncellenir.

## İyileştirme Önerileri
1. **Ödeme Gateway**: Gerçek ödeme entegrasyonu
2. **SMS Bildirim**: İşlem adımları için SMS
3. **Email Bildirim**: Sözleşme PDF gönderimi
4. **Belge Yükleme**: Gerekli belgelerin yüklenmesi
5. **Signature Canvas**: Gerçek imza alma
6. **Araç Detay**: Daha fazla araç bilgisi gösterimi
7. **Fotoğraf Galerisi**: Araç fotoğraflarının gösterimi

## Sorun Giderme

### Sorun: Araç listesi gelmiyor
**Çözüm**: Tarih seçimini kontrol edin, backend'deki araçların `state` değerinin `1` olduğundan emin olun.

### Sorun: Sözleşme oluşturulamıyor
**Çözüm**: 
1. Console'daki `Creating contract with` log'una bakın
2. `rentalId` değerinin null olmadığını kontrol edin
3. Backend'deki `ContractServiceImpl` log'larına bakın

### Sorun: Ödeme kaydedilemiyor
**Çözüm**: `rentalId` değerinin null olmadığını kontrol edin, `customerId` değerinin doğru olduğunu kontrol edin.

## Test Senaryoları

### Test 1: Happy Path
1. Tarih seç → Araç seç → Şablon seç → Özeti incele → Öde
2. Beklenen: Tüm adımlar başarılı

### Test 2: Validasyon
1. Araç seçme → İlerle
2. Beklenen: "Lütfen araç ve tarih seçin" mesajı

### Test 3: Geri Dön
1. 3. adıma git → Geri
2. Beklenen: 2. adıma dön

### Test 4: Refresh
1. İşlem tamamla → Portala dön
2. Beklenen: Kiralama listesinde yeni kayıt görünür

## Güvenlik Notları
- Tüm istekler JWT token ile korunur
- Müşteri kendi bilgilerini görür
- Sadece available araçlar gösterilir
- Date picker'da geçmiş tarih seçilemez
- Backend'de tüm validasyonlar yapılır

## Performans Notları
- Araç listesi sadece gerektiğinde yüklenir (enabled condition)
- Sözleşme şablonları cache'lenir
- İşlemler sırayla yapılır (zincirleme mutation)
- Loading state'ler ile kullanıcıya feedback verilir

## Değişiklik Geçmişi
- **2025-01-XX**: İlk versiyon oluşturuldu
  - Araç seçimi
  - Sözleşme şablonu
  - Sözleşme özeti
  - Ödeme bilgileri
  - Tamamlanma ekranı


