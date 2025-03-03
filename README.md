# Migros Courier Tracking Application 🚚

## 📝 Proje Açıklaması

Bu uygulama, kuryelerin Migros mağazaları çevresindeki hareketlerini takip eden ve toplam kat ettikleri mesafeyi hesaplayan bir Spring Boot uygulamasıdır.

## ✨ Özellikler

- 📍 Kurye konum takibi
- 🏪 Mağaza giriş/çıkış loglama
- 📏 Toplam mesafe hesaplama
- 🎯 Mağaza yakınlık kontrolü (100 metre yarıçap)
- ⏱️ Mükerrer giriş kontrolü (1 dakika kuralı - aynı mağazaya 1 dakika içinde yapılan tekrar girişler kaydedilmez)

## 🛠️ Hızlı Başlangıç Scripti

Projeyi daha kolay çalıştırmak için `run.sh` script'ini kullanabilirsiniz:

1. Script'i çalıştırılabilir yapın:
- `chmod +x run.sh`

2. Kullanılabilir komutlar:

- `./run.sh run`           # Uygulamayı çalıştır
- `./run.sh test`         # Tüm testleri çalıştır
- `./run.sh build`        # Projeyi derle
- `./run.sh test-location` # Konum servis testlerini çalıştır
- `./run.sh test-store`   # Mağaza servis testlerini çalıştır

## 🔧 Gereksinimler

- Java 21
- Maven 3.6 veya üzeri

## 🚀 Kurulum ve Çalıştırma

1. Projeyi klonlayın:
- `bash`
- `git clone https://github.com/SerkanKonus/courier.git`
- `cd courier`

2. Maven ile derleyin ve çalıştırın:
- `bash`
- `mvn clean install`
- `mvn spring-boot:run`

## 📚 API Dokümantasyonu

Swagger UI'a aşağıdaki URL üzerinden erişebilirsiniz:
http://localhost:8080/swagger-ui/index.html

API Spesifikasyonunu (OpenAPI) JSON formatında görüntülemek için:
http://localhost:8080/v3/api-docs

## 🔌 API Endpoints

Uygulama `http://localhost:8080` adresinde çalışır.

### 1. Kurye Konumu Kaydetme

#### Endpoint
- **URL**: `/api/v1/courier/location`
- **Metod**: `POST`
- **İçerik Tipi**: `application/json`


#### İstek Gövdesi:
```
{
  "courierId": "c1",
  "lat": 40.9923307,
  "lng": 29.1244229,
  "time": "2024-03-03T10:00:00"
}
```

### 2. Kurye Toplam Mesafesini Sorgulama
- **URL**: `/api/v1/courier/{courierId}/total-travel-distance`
- **Metod**: `GET`

### 3. Kurye Giriş Yaptığı Mağazaları Görüntüleme
- **URL**: `/api/v1/courier/{courierId}/entries`
- **Metod**: `GET`

### 4. Tüm Migros Mağazalarını Listeleme

- **URL**: `/api/v1/store`
- **Metod**: `GET`


## 🧪 Test Etme

### Tüm Testleri Çalıştırma
- `bash`
- `mvn test`

### Belirli Test Sınıflarını Çalıştırma
- `bash`
- `mvn test -Dtest=LocationTrackingServiceTest`
- `mvn test -Dtest=StoreServiceTest`

## 📁 Proje Yapısı

```
src
├── main
│   ├── java
│   │   └── com.migros.courier
│   │       ├── controller
│   │       │   ├── CourierController.java
│   │       │   └── StoreController.java
│   │       ├── service
│   │       │   ├── LocationTrackingService.java
│   │       │   └── StoreService.java
│   │       ├── repository
│   │       │   ├── CourierEntryRepository.java
│   │       │   └── StoreRepository.java
│   │       ├── model
│   │       │   ├── CourierLocation.java
│   │       │   ├── Store.java
│   │       │   └── CourierEntry.java
│   │       └── dto
│   │           └── CourierEntryResponse.java
│   └── resources
│       └── application.yml
└── test
    └── java
        └── com.migros.courier
            └── service
                ├── LocationTrackingServiceTest.java
                ├── StoreServiceTest.java
                └── TestConstants.java
```


## 🧪 Test Kapsamı

Uygulama aşağıdaki test senaryolarını içerir:

- ✅ Kurye konum takibi ve mesafe hesaplama testleri
- ✅ Mağaza yakınlık kontrolü testleri (100m yarıçap)
- ✅ Mağaza giriş loglama testleri
- ✅ Mükerrer giriş kontrolü testleri (1 dakika kuralı)
- ✅ Toplam mesafe hesaplama testleri

## 📝 Geliştirme Notları

- 🧮 Mesafe hesaplamaları Haversine formülü kullanılarak yapılmaktadır
- 💾 Mağaza giriş kayıtları in-memory veritabanında tutulmaktadır
- ⏰ Mükerrer giriş kontrolü için 1 dakikalık süre kısıtı uygulanmaktadır
- 📍 Mağaza yakınlık tespiti 100 metre yarıçap içinde yapılmaktadır
