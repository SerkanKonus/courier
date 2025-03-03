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

## 🛠 Teknolojiler & Araçlar

### Ana Teknolojiler
- ![Java](https://img.shields.io/badge/Java-21-orange)
- ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
- ![H2 Database](https://img.shields.io/badge/H2%20Database-2.x-blue)
- ![Maven](https://img.shields.io/badge/Maven-3.x-red)

### Yardımcı Kütüphaneler
- ![Lombok](https://img.shields.io/badge/Lombok-1.18.x-red) - Kod tekrarını azaltmak için
- ![Swagger](https://img.shields.io/badge/Swagger-3.x-green) - API dokümantasyonu için
- ![Spring Boot Test](https://img.shields.io/badge/Spring%20Boot%20Test-3.x-brightgreen) - Test framework'ü
- ![AssertJ](https://img.shields.io/badge/AssertJ-3.x-yellow) - Test assertions için

## 🔍 Özellikler
- In-memory H2 veritabanı kullanımı
- Exception handling:
  - Global exception handler ile merkezi hata yönetimi
  - Özel exception sınıfları (CourierNotFoundException, DuplicateStoreEntryException, StoreNotFoundException)
- Swagger UI ile API dokümantasyonu (/swagger-ui.html)
- Spring Boot Test ve AssertJ ile servis katmanı testleri

## 🚀 Kurulum ve Çalıştırma

1. Projeyi klonlayın:
```bash
git clone https://github.com/SerkanKonus/courier.git
cd courier
```

2. Maven ile derleyin ve çalıştırın:

```bash
mvn clean install
mvn spring-boot:run
```

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
│   │       │   ├── CourierLocationRepository.java
│   │       │   └── StoreRepository.java
│   │       ├── model
│   │       │   ├── base
│   │       │   │   └── BaseEntity.java
│   │       │   ├── CourierLocation.java
│   │       │   ├── Store.java
│   │       │   └── CourierEntry.java
│   │       ├── dto
│   │       │   └── CourierEntryResponse.java
│   │       ├── exception
│   │       │   ├── LocationTrackingException.java
│   │       │   ├── StoreEntryException.java
│   │       │   └── StoreInitializationException.java
│   │       ├── util
│   │       │    └── DistanceCalculator.java
│   │       └── config
│   │           └── SwaggerConfig.java
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

## 🎨 Kullanılan Design Pattern'ler

### Creational Patterns
- 🏭 **Singleton Pattern**: Spring framework'ün sağladığı default davranış ile @Service ve @Repository annotasyonlu sınıflar (LocationTrackingService, StoreService, CourierEntryRepository, StoreRepository) singleton olarak yönetilmektedir.

### Structural Patterns
- 🔄 **DTO Pattern**: CourierEntryResponse sınıfı ile domain modellerinin (CourierEntry) API response'larına dönüştürülmesinde kullanılmıştır.
- 🎯 **Repository Pattern**: CourierEntryRepository ve StoreRepository ile veri erişim katmanının soyutlanması sağlanmıştır.

### Architectural Patterns
- 📱 **MVC Pattern**: Controller-Service-Repository katmanları ile uygulama sorumlulukları ayrılmıştır.
- 🔲 **Layered Architecture**: Presentation (Controller), Business (Service) ve Data (Repository) katmanları ile uygulama modüler bir yapıda tasarlanmıştır.
