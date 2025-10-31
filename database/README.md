# RentACar Projesi Veritabanı

## Örnek Veri Kullanımı

Bu dizinde bulunan `sample_data.sql` dosyası RentACar projeniz için hazırlanmış örnek verileri içerir.

### İçerik

- **10 Marka** (Toyota, Ford, BMW, Mercedes-Benz, Audi, Volkswagen, Honda, Hyundai, Renault, Peugeot)
- **26 Model** (Her marka için 2-3 model)
- **26 Araç** (Farklı plakalar, fiyatlar ve durumlar)
- **10 Müşteri** (Gerçekçi Türkçe isimler ve bilgiler)
- **10 Kiralama** (Geçmiş ve aktif kiralama kayıtları)

### Kurulum

1. PostgreSQL veritabanınızı oluşturun
2. Spring Boot uygulamanızı çalıştırarak tabloları otomatik oluşturun
3. Bu SQL dosyasını çalıştırın:

```bash
psql -h localhost -U kullanici_adi -d rentacar_db -f sample_data.sql
```

veya pgAdmin gibi bir GUI araç kullanarak dosyayı import edin.

### Notlar

- Tüm tarihler 2024 yılı için güncel tutulmuştur
- Araç durumları: 1-Müsait, 2-Kiralandı, 3-Bakımda
- TC Kimlik numaraları ve telefon numaraları örnek formatındadır
- Günlük kiralama fiyatları 350-1200 TL arasında değişmektedir


