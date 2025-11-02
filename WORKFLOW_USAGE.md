# Kiralama Workflow Kullanım Kılavuzu 🚀

## 📍 Workflow Nerede Devreye Giriyor?

Yeni kiralama akışı sistemin 3 noktasından erişilebilir:

### 1️⃣ Sol Menüden (`Layout.tsx`)
**Konum:** Sol sidebar - "Yeni Kiralama" menü öğesi
- ✅ Menüde üst sıralarda, müşteriler ve araçlar arasında
- ✅ İkon: `<AddIcon />` (Yeni ekle)
- ✅ Path: `/rental-workflow`

```typescript
{ text: 'Yeni Kiralama', icon: <AddIcon />, path: '/rental-workflow' }
```

### 2️⃣ Kiralama Sayfasından (`RentalsPage.tsx`)
**Konum:** Kiralama listesi sayfasının üst kısmı
- ✅ Buton: "Yeni Kiralama (Workflow)"
- ✅ Eylem: Eski dialog yerine workflow sayfasına yönlendirir
- ✅ Güncelleme: Artık eski `handleOpen()` yerine `navigate('/rental-workflow')` kullanır

```typescript
<Button
  variant="contained"
  color="primary"
  startIcon={<AddIcon />}
  onClick={() => navigate('/rental-workflow')}
>
  Yeni Kiralama (Workflow)
</Button>
```

### 3️⃣ URL'den Direkt Erişim
**URL:** `http://localhost:3000/rental-workflow`
- ✅ Kimlik doğrulama gerekli (Protected Route)
- ✅ Sadece admin kullanıcılar erişebilir

## 🎯 Workflow'u Başlatma

### Adım 1: Erişim
1. **Menüden:** Sol menüden "Yeni Kiralama" tıklayın
2. **Kiralama Sayfasından:** "Yeni Kiralama (Workflow)" butonuna tıklayın
3. **Direkt:** `/rental-workflow` URL'sine gidin

### Adım 2: Kullanım
Workflow otomatik olarak 5 adımda işlem yapar:
1. **Araç ve Müşteri Seçimi** → Otomatik kiralama kaydı oluşturur
2. **Sözleşme Şablonu** → Otomatik sözleşme oluşturur
3. **Sözleşme İmzalama** → Otomatik imza ve ödeme kaydı oluşturur
4. **Teslim Kontrolü** → Otomatik durum kaydı oluşturur
5. **Tamamlama** → `/rentals` sayfasına yönlendirir

## 🔄 İş Akışı Karşılaştırması

### ❌ Eski Yöntem (Dialog-based)
```
Kiralama Sayfası
  ↓
"Yeni Kiralama" Butonu
  ↓
Dialog Açılır
  ↓
Form Doldur
  ↓
Kaydet
  ↓
Kiralama Listesi Güncellenir
```

### ✅ Yeni Workflow (Step-by-step)
```
Menü/Kiralama Sayfası
  ↓
"Yeni Kiralama" / "Yeni Kiralama (Workflow)"
  ↓
Workflow Sayfası Açılır
  ↓
Adım 1: Araç + Müşteri → Kiralama Oluştur
  ↓
Adım 2: Şablon → Sözleşme Oluştur
  ↓
Adım 3: Sözleşme → İmzala + Ödeme
  ↓
Adım 4: Teslim Kontrolü → Durum Kaydı
  ↓
Tamamlandı → Kiralama Listesine Dön
```

## 🎨 UI/UX İyileştirmeleri

### Avantajlar
- ✅ **Görsel İlerleme:** Stepper ile adım adım takip
- ✅ **Rehberli Süreç:** Her adımda ne yapacağınızı görürsünüz
- ✅ **Otomatikleştirme:** Her adımda backend işlemleri otomatik
- ✅ **Hata Önleme:** Validasyonlar her adımda
- ✅ **Gerçek Zamanlı Feedback:** Snackbar ile anlık bildirimler

### Eski vs Yeni
| Özellik | Eski Yöntem | Yeni Workflow |
|---------|-------------|---------------|
| UI Tipi | Dialog (Modal) | Full Page |
| Adım Sayısı | 1 Form | 5 Adım |
| Otomatik İşlemler | ❌ Manuel | ✅ Otomatik |
| İlerleme Takibi | ❌ Yok | ✅ Stepper |
| Geri Dönüş | ❌ Zor | ✅ Kolay |
| Hata Yönetimi | ⚠️ Basit | ✅ Detaylı |

## 📊 Workflow Adımları Detayı

### 1️⃣ Araç ve Tarih Seçimi
**Ne Yapar:**
- Tarih aralığı seçimi
- Müsait araçların listelenmesi
- Müşteri seçimi
- Otomatik kiralama kaydı oluşturma

**Backend İşlemleri:**
```typescript
POST /api/rentals
{
  carId: selectedCar.id,
  customerId: selectedCustomer,
  start: startDate,
  end: endDate,
  totalAmount: calculatedAmount,
  status: 'CONFIRMED'
}
```

### 2️⃣ Sözleşme Şablonu
**Ne Yapar:**
- Mevcut şablonları listeler
- Kullanıcı seçim yapar
- Otomatik sözleşme oluşturur

**Backend İşlemleri:**
```typescript
POST /api/contracts
{
  rentalId: createdRentalId,
  customerId: selectedCustomer,
  templateId: selectedTemplate,
  status: 'DRAFT'
}
```

### 3️⃣ Sözleşme İmzalama
**Ne Yapar:**
- Sözleşme görüntülenir
- İmzalar alınır (şimdilik simüle)
- Ödeme kaydı oluşturulur

**Backend İşlemleri:**
```typescript
POST /api/contracts/{id}/sign
{
  customerSignature: "signature",
  companySignature: "signature"
}

POST /api/payments
{
  rentalId: rentalId,
  customerId: customerId,
  amount: totalAmount,
  method: paymentMethod,
  status: 'COMPLETED'
}
```

### 4️⃣ Teslim Kontrolü
**Ne Yapar:**
- Araç durumu kontrolü
- Kilometre/Yakıt kaydı
- Fotoğraf yükleme (opsiyonel)
- Durum kaydı oluşturur

**Backend İşlemleri:**
```typescript
POST /api/vehicle-condition-checks
{
  rentalId: rentalId,
  carId: carId,
  checkType: 'TESLIM',
  mileageAtCheck: mileage,
  fuelLevel: 100,
  // ... diğer alanlar
}
```

### 5️⃣ Tamamlama
**Ne Yapar:**
- Başarı mesajı gösterir
- 2 saniye bekler
- Kiralama listesine yönlendirir

## 🔐 Yetkilendirme

**Kimler Kullanabilir:**
- ✅ Admin kullanıcılar
- ✅ Sol menüden görünür
- ✅ Protected Route koruması altında

**Kimler Kullanamaz:**
- ❌ Normal kullanıcılar
- ❌ Misafir kullanıcılar
- ❌ Giriş yapmamış kullanıcılar

## 🚨 Hata Senaryoları

### Senaryo 1: Araç Müsait Değil
**Ne Olur:**
1. Sistem uyarı verir
2. Farklı araç veya tarih seçmenizi ister
3. İşlemi tekrarlayabilirsiniz

### Senaryo 2: Müşteri Seçilmedi
**Ne Olur:**
1. "Devam Et" butonu disabled
2. Hata mesajı görürsünüz
3. Adımdan ilerleyemezsiniz

### Senaryo 3: Sözleşme Oluşturma Hatası
**Ne Olur:**
1. Geri dönüp tekrar deneyebilirsiniz
2. Hata mesajı Snackbar'da görünür
3. Tamamlanan işlemler korunur

## 📝 Öneriler

### Kullanıcılar İçin
1. **Dikkatli Olun:** Her adımı gözden geçirin
2. **Validasyonları Okuyun:** Form hatalarını kontrol edin
3. **Geri Dönün:** Hata durumunda "Geri" butonunu kullanın
4. **Tamamlayın:** İşlemi yarıda bırakmayın

### Geliştiriciler İçin
1. **Backend API'ler:** Tüm endpoint'ler hazır olmalı
2. **Database:** Migration'lar uygulanmalı
3. **Mock Data:** Test için veri olmalı
4. **Logs:** Hata takibi için log açmalısınız

## 🎯 Gelecek Geliştirmeler

### Kısa Vadede
- [ ] Gerçek fotoğraf yükleme
- [ ] E-imza entegrasyonu
- [ ] Fatura PDF oluşturma

### Orta Vadede
- [ ] Mobil optimizasyon
- [ ] Çevrimdışı destek
- [ ] Bildirim entegrasyonu

### Uzun Vadede
- [ ] AI destekli durum analizi
- [ ] Blockchain ile güvenli imza
- [ ] Video konferans entegrasyonu

## ✅ Test Edilmesi Gerekenler

### Başarılı Senaryolar
1. ✅ Araç seçimi ve kiralama oluşturma
2. ✅ Sözleşme oluşturma ve imzalama
3. ✅ Ödeme kaydı oluşturma
4. ✅ Teslim kontrolü kaydı
5. ✅ Tamamlama ve yönlendirme

### Hata Senaryoları
1. ⚠️ Araç müsait değil
2. ⚠️ Network hatası
3. ⚠️ Validation hataları
4. ⚠️ Backend timeout

### Edge Cases
1. ⚠️ Aynı anda birden fazla işlem
2. ⚠️ Session timeout
3. ⚠️ Browser refresh

---

**🎉 Workflow Başarıyla Entegre Edildi!**

Artık sistemin 3 farklı noktasından yeni kiralama işlemi başlatabilirsiniz!


