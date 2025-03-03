package com.migros.courier.service;

import com.migros.courier.exception.LocationTrackingException;
import com.migros.courier.model.CourierLocation;
import com.migros.courier.model.Store;
import com.migros.courier.repository.CourierLocationRepository;
import com.migros.courier.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kurye lokasyon takibi ve mağaza ziyaretlerini yöneten servis.
 * Thread-safe implementasyon ile concurrent işlemleri destekler.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationTrackingService {
    private static final double STORE_RADIUS_METERS = 100.0;

    private final StoreService storeService;
    private final CourierLocationRepository courierLocationRepository;

    // Thread-safe önbellekler
    private final Map<String, CourierLocation> lastLocations = new ConcurrentHashMap<>();

    /**
     * Kurye lokasyonunu takip eder ve ilgili işlemleri gerçekleştirir.
     * 1. Toplam mesafeyi günceller
     * 2. Mağaza ziyaretlerini kontrol eder
     * 3. Lokasyonu veritabanına kaydeder
     *
     * @param location Kurye lokasyon bilgisi
     * @throws RuntimeException lokasyon takibi başarısız olduğunda
     */
    @Transactional
    public void trackLocation(CourierLocation location) {
        validateCoordinates(location);  // Koordinat validasyonu eklendi
        try {
            log.debug("Konum takibi başladı - Kurye: {}", location.getCourierId());

            // Toplam mesafeyi güncelle
            updateTotalDistance(location);

            // Yakındaki mağazaları kontrol et
            List<Store> nearbyStores = findNearbyStores(location);

            // Yakında mağaza varsa ziyaretleri işle
            if (!nearbyStores.isEmpty()) {
                log.debug("Kurye {} için {} mağaza yakında bulundu",
                        location.getCourierId(), nearbyStores.size());
                processStoreVisits(nearbyStores, location);
            }

            // Konumu kaydet
            saveLocation(location);

        } catch (Exception e) {
            log.error("Konum takibi başarısız - Kurye: {}", location.getCourierId(), e);
            throw new LocationTrackingException(
                    String.format("Kurye konumu takip edilemedi. Kurye ID: %s", location.getCourierId()),
                    e
            );
        }
    }

    /**
     * Kuryenin kat ettiği toplam mesafeyi günceller.
     * Son konum ile yeni konum arasındaki mesafeyi hesaplar.
     *
     * @param currentLocation Güncel kurye konumu
     */
    private void updateTotalDistance(CourierLocation currentLocation) {
        CourierLocation previousLocation = lastLocations.get(currentLocation.getCourierId());

        if (previousLocation != null) {
            double distance = DistanceCalculator.calculateDistance(
                    previousLocation.getLat(), previousLocation.getLng(),
                    currentLocation.getLat(), currentLocation.getLng()
            );

            if (distance > 0) {
                storeService.updateTotalDistance(currentLocation.getCourierId(), distance);
                log.debug("Mesafe güncellendi - Kurye: {}, Ek mesafe: {}m",
                        currentLocation.getCourierId(), distance);
            }
        }

        lastLocations.put(currentLocation.getCourierId(), currentLocation);
    }

    /**
     * Kurye konumuna yakın mağazaları kontrol eder ve ziyaretleri işler.
     *
     * @param location Kurye konumu
     */
    private void processStoreVisits(List<Store> nearbyStores, CourierLocation location) {
        for (Store store : nearbyStores) {
            if (!storeService.hasRecentEntry(store, location.getCourierId(), location.getTimestamp())) {
                storeService.logStoreEntry(store, location);
                log.info("Mağaza ziyareti kaydedildi - Kurye: {}, Mağaza: {}, Zaman: {}",
                        location.getCourierId(), store.getName(), location.getTimestamp());
            }
        }
    }

    /**
     * Kurye konumuna yakın (100m yarıçap içindeki) mağazaları bulur.
     *
     * @param location Kurye konumu
     * @return Yakındaki mağazaların listesi
     */
    private List<Store> findNearbyStores(CourierLocation location) {
        return storeService.getAllStores().stream()
                .filter(store -> isWithinRadius(location, store))
                .toList();
    }

    /**
     * Kurye konumunun mağaza yarıçapı (100m) içinde olup olmadığını kontrol eder.
     *
     * @param location Kurye konumu
     * @param store    Mağaza
     * @return true: yarıçap içinde, false: yarıçap dışında
     */
    private boolean isWithinRadius(CourierLocation location, Store store) {
        double distance = DistanceCalculator.calculateDistance(
                location.getLat(), location.getLng(),
                store.getLat(), store.getLng()
        );
        return distance <= STORE_RADIUS_METERS;
    }

    /**
     * Kurye konumunu veritabanına kaydeder.
     *
     * @param location Kaydedilecek kurye konumu
     */
    private void saveLocation(CourierLocation location) {
        courierLocationRepository.save(location);
        log.debug("Konum kaydedildi - Kurye: {}, Zaman: {}",
                location.getCourierId(), location.getTimestamp());
    }

    /**
     * Kurye konum koordinatlarının geçerliliğini kontrol eder.
     * Enlem değeri -90° ile +90° arasında,
     * Boylam değeri -180° ile +180° arasında olmalıdır.
     *
     * @param location Kontrol edilecek kurye konumu
     * @throws IllegalArgumentException koordinatlar geçerli aralıkta değilse
     */
    private void validateCoordinates(CourierLocation location) {
        if (location.getLat() < -90 || location.getLat() > 90) {
            throw new IllegalArgumentException("Enlem değeri -90 ile 90 derece arasında olmalıdır");
        }
        if (location.getLng() < -180 || location.getLng() > 180) {
            throw new IllegalArgumentException("Boylam değeri -180 ile 180 derece arasında olmalıdır");
        }
    }
}
