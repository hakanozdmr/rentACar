# 🔍 Audit Log Stratejisi - Rent A Car Sistemi

## 📋 Genel Bakış

Audit Log sistemi, sistemde gerçekleşen kritik işlemleri izlemek ve güvenlik, uyumluluk ve iş süreçlerini takip etmek için kullanılır.

## 🎯 Audit Log Ne Zaman Atılmalı?

### 🔴 KRİTİK İŞLEMLER (Mutlaka Audit Gerekli)

#### **1. Veri Değişiklikleri**
- ✅ **CREATE**: Yeni kayıt oluşturma (Car, Customer, Reservation, User, vb.)
- ✅ **UPDATE**: Mevcut kayıtları güncelleme
- ✅ **DELETE**: Kayıt silme işlemleri

#### **2. Kimlik Doğrulama**
- ✅ **LOGIN**: Kullanıcı giriş işlemleri
- ✅ **LOGOUT**: Kullanıcı çıkış işlemleri
- ✅ **ACCESS_DENIED**: Yetkisiz erişim denemeleri

#### **3. İş Süreçleri**
- ✅ **Rezervasyon Onaylama**: `confirmReservation()`
- ✅ **Rezervasyon İptali**: `cancelReservation()`
- ✅ **Kiralama Başlatma**: `startRental()`
- ✅ **Kiralama Bitirme**: `endRental()`

#### **4. Para İşlemleri**
- ✅ **Ödeme İşlemleri**: Payment create/update/delete
- ✅ **Fatura İşlemleri**: Invoice operations
- ✅ **Vergi Hesaplamaları**: Tax calculations

### 🟡 ORTA SEVIYE İŞLEMLER (Önemli)

#### **1. Raporlama**
- ⚠️ **EXPORT**: Veri dışa aktarma
- ⚠️ **IMPORT**: Veri içe aktarma

#### **2. Sistem Yönetimi**
- ⚠️ **BACKUP**: Sistem yedekleme
- ⚠️ **RESTORE**: Sistem geri yükleme

### 🟢 DÜŞÜK SEVIYE İŞLEMLER (Opsiyonel)

#### **1. Okuma İşlemleri**
- 📖 **READ**: Normal veri okuma (sadece hassas veriler için)

## 🛠️ Teknik Implementasyon

### **@Auditable Annotation Kullanımı**

```java
@Auditable(entity = "EntityName", action = AuditLog.ActionType.ACTION_TYPE, description = "Açıklama")
```

### **Mevcut ActionType'lar:**
```java
public enum ActionType {
    CREATE("Oluşturuldu"),
    UPDATE("Güncellendi"), 
    DELETE("Silindi"),
    READ("Okundu"),
    LOGIN("Giriş"),
    LOGOUT("Çıkış"),
    ACCESS_DENIED("Erişim Reddedildi"),
    EXPORT("Dışa Aktarıldı"),
    IMPORT("İçe Aktarıldı"),
    BACKUP("Yedekleme"),
    RESTORE("Geri Yükleme"),
    BULK_UPDATE("Toplu Güncelleme"),
    BULK_DELETE("Toplu Silme")
}
```

## 📝 Service Metodları için Audit Log Örnekleri

### **1. Car Service**
```java
@Override
@Auditable(entity = "Car", action = AuditLog.ActionType.CREATE, description = "Create new car")
public CarDto add(CarDto carDto) { ... }

@Override
@Auditable(entity = "Car", action = AuditLog.ActionType.UPDATE, description = "Update car information")
public CarDto update(CarDto carDto) { ... }

@Override
@Auditable(entity = "Car", action = AuditLog.ActionType.DELETE, description = "Delete car")
public CarDto delete(Long id) { ... }
```

### **2. Customer Service**
```java
@Override
@Auditable(entity = "Customer", action = AuditLog.ActionType.CREATE, description = "Create new customer")
public CustomerDto add(CustomerDto customerDto) { ... }

@Override
@Auditable(entity = "Customer", action = AuditLog.ActionType.UPDATE, description = "Update customer information")
public CustomerDto update(CustomerDto customerDto) { ... }

@Override
@Auditable(entity = "Customer", action = AuditLog.ActionType.DELETE, description = "Delete customer")
public CustomerDto delete(Long id) { ... }
```

### **3. Auth Service**
```java
@Override
@Auditable(entity = "User", action = AuditLog.ActionType.LOGIN, description = "User login")
public LoginResponse login(LoginRequest loginRequest) { ... }

@Override
@Auditable(entity = "User", action = AuditLog.ActionType.CREATE, description = "User registration")
public UserDto register(UserDto userDto) { ... }
```

### **4. Reservation Service**
```java
@Override
@Auditable(entity = "Reservation", action = AuditLog.ActionType.CREATE, description = "Create new reservation")
public ReservationDto create(ReservationDto reservationDto) { ... }

@Override
@Auditable(entity = "Reservation", action = AuditLog.ActionType.UPDATE, description = "Update reservation")
public ReservationDto update(ReservationDto reservationDto) { ... }

@Override
@Auditable(entity = "Reservation", action = AuditLog.ActionType.UPDATE, description = "Confirm reservation")
public ReservationDto confirmReservation(Long reservationId) { ... }
```

## 📊 Audit Log Kaydı İçeriği

Audit log her kayıt şunları içerir:

### **Temel Bilgiler:**
- `entityName`: İşlem yapılan varlık (Car, Customer, vb.)
- `entityId`: Varlık ID'si
- `actionType`: Yapılan işlem türü
- `userId`: İşlemi yapan kullanıcı ID'si
- `username`: Kullanıcı adı
- `timestamp`: İşlem zamanı

### **Request Detayları:**
- `ipAddress`: Kullanıcı IP adresi
- `userAgent`: Tarayıcı bilgisi
- `requestMethod`: HTTP metodu (GET, POST, PUT, DELETE)
- `requestUrl`: İstek URL'i

### **İşlem Detayları:**
- `oldValues`: Eski değerler (JSON)
- `newValues`: Yeni değerler (JSON)
- `changedFields`: Değişen alanlar
- `operationResult`: İşlem sonucu (SUCCESS, FAILURE, ERROR)
- `errorMessage`: Hata mesajı (varsa)
- `executionTimeMs`: İşlem süresi

## 🔒 Güvenlik ve Performans

### **Güvenlik:**
- Tüm audit loglar sadece ADMIN yetkisine sahip kullanıcılar görebilir
- Hassas veriler (şifreler) audit logda saklanmaz

### **Performans:**
- Audit log işlemleri asenkron olarak çalışır
- Büyük miktarda audit log birikimini önlemek için cleanup işlemi mevcuttur

## 📋 Eksik Audit Log Alanları

Aşağıdaki service metodlarına audit log eklenmelidir:

### **Rental Service:**
- `startRental()` - LOGIN benzeri işlem
- `endRental()` - UPDATE işlemi

### **Payment Service:**
- `create()`, `update()`, `delete()` metodları

### **Invoice Service:**
- `create()`, `update()`, `delete()` metodları
- `markAsPaid()`, `markAsSent()` metodları

### **Notification Service:**
- Kritik bildirim gönderme işlemleri

### **Email Service:**
- Önemli email gönderme işlemleri

## 🎯 Sonuç

Audit logging sistemi, sistem güvenliği ve uyumluluğu için kritik öneme sahiptir. Yukarıdaki stratejiye göre tüm kritik işlemler audit edilmeli ve düzenli olarak izlenmelidir.




