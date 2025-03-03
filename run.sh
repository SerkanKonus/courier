#!/bin/bash

case "$1" in
  "run")
    echo "Uygulama başlatılıyor..."
    mvn spring-boot:run
    ;;
  "test")
    echo "Testler çalıştırılıyor..."
    mvn test
    ;;
  "build")
    echo "Proje derleniyor..."
    mvn clean install
    ;;
  "test-location")
    echo "LocationTrackingService testleri çalıştırılıyor..."
    mvn test -Dtest=LocationTrackingServiceTest
    ;;
  "test-store")
    echo "StoreService testleri çalıştırılıyor..."
    mvn test -Dtest=StoreServiceTest
    ;;
  *)
    echo "Kullanım: ./run.sh [komut]"
    echo "Komutlar:"
    echo "  run           - Uygulamayı çalıştır"
    echo "  test         - Tüm testleri çalıştır"
    echo "  build        - Projeyi derle"
    echo "  test-location - Konum servis testlerini çalıştır"
    echo "  test-store   - Mağaza servis testlerini çalıştır"
    ;;
esac 