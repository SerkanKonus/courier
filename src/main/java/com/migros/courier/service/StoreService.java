package com.migros.courier.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.migros.courier.dto.CourierEntryResponse;
import com.migros.courier.exception.StoreEntryException;
import com.migros.courier.exception.StoreInitializationException;
import com.migros.courier.model.CourierEntry;
import com.migros.courier.model.CourierLocation;
import com.migros.courier.model.Store;
import com.migros.courier.repository.CourierEntryRepository;
import com.migros.courier.repository.StoreRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mağaza işlemlerini ve kurye mesafe takibini yöneten servis.
 * Thread-safe implementasyon ile concurrent işlemleri destekler.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {
    private static final String STORES_JSON_PATH = "stores.json";
    private static final String STORES_CACHE_NAME = "stores";

    private final StoreRepository storeRepository;
    private final CourierEntryRepository courierEntryRepository;
    private final ObjectMapper objectMapper;

    @Value("${store.visit.min-interval-minutes}")
    private int minVisitIntervalMinutes;

    // Thread-safe mesafe takibi için ConcurrentHashMap
    private final Map<String, Double> courierDistances = new ConcurrentHashMap<>();

    /**
     * Uygulama başlatıldığında mağaza verilerini JSON dosyasından yükler.
     * Veritabanı boşsa mağazaları kaydeder.
     */
    @PostConstruct
    public void initializeStores() {
        try {
            if (isStoreDataEmpty()) {
                List<Store> stores = loadStoresFromJson();
                saveStores(stores);
                log.info("Mağaza verileri başarıyla yüklendi. Toplam {} mağaza.", stores.size());
            }
        } catch (IOException e) {
            log.error("Mağaza verileri yüklenirken hata oluştu", e);
            throw new StoreInitializationException("Mağaza verileri yüklenemedi", e);
        }
    }

    private boolean isStoreDataEmpty() {
        return storeRepository.count() == 0;
    }

    private List<Store> loadStoresFromJson() throws IOException {
        return objectMapper.readValue(
                new ClassPathResource(STORES_JSON_PATH).getInputStream(),
                new TypeReference<>() {
                }
        );
    }

    private void saveStores(List<Store> stores) {
        storeRepository.saveAll(stores);
    }

    /**
     * Tüm mağazaları getirir. Performans için cache kullanır.
     *
     * @return Mağaza listesi
     */
    @Cacheable(STORES_CACHE_NAME)
    @Transactional(readOnly = true)
    public List<Store> getAllStores() {
        try {
            return storeRepository.findAll();
        } catch (Exception e) {
            log.error("Mağaza verileri getirilirken hata oluştu", e);
            return Collections.emptyList();
        }
    }

    /**
     * Kurye'nin kat ettiği toplam mesafeyi günceller.
     * Thread-safe implementasyon.
     *
     * @param courierId          Kurye ID
     * @param additionalDistance Eklenen mesafe (metre)
     */
    public void updateTotalDistance(String courierId, double additionalDistance) {
        if (courierId == null || courierId.trim().isEmpty()) {
            throw new IllegalArgumentException("Kurye ID boş olamaz");
        }

        if (additionalDistance < 0) {
            log.warn("Negatif mesafe değeri: {} for courier: {}", additionalDistance, courierId);
            return;
        }

        courierDistances.compute(courierId.trim(), (k, v) -> {
            double currentDistance = (v == null) ? 0 : v;
            double newDistance = currentDistance + additionalDistance;
            log.debug("Kurye {} için mesafe güncellendi: {} -> {}",
                    courierId, currentDistance, newDistance);
            return newDistance;
        });
    }

    /**
     * Kurye'nin toplam kat ettiği mesafeyi getirir.
     *
     * @param courierId Kurye ID
     * @return Toplam mesafe (metre)
     */
    @Transactional(readOnly = true)
    public Double getTotalTravelDistance(String courierId) {
        return courierDistances.getOrDefault(courierId, 0.0);
    }

    /**
     * Kurye'nin mağaza ziyaretini kaydeder.
     *
     * @param store    Ziyaret edilen mağaza
     * @param location Kurye konumu
     */
    @Transactional
    public void logStoreEntry(Store store, CourierLocation location) {
        try {
            CourierEntry entry = createCourierEntry(store, location);
            courierEntryRepository.save(entry);

            log.info("Kurye {} {} mağazasına girdi. Zaman: {}",
                    location.getCourierId(),
                    store.getName(),
                    location.getTimestamp());

        } catch (Exception e) {
            log.error("Mağaza girişi kaydedilirken hata oluştu. Kurye: {}, Mağaza: {}",
                    location.getCourierId(), store.getName(), e);
            throw new StoreEntryException("Mağaza girişi kaydedilemedi", e);
        }
    }

    private CourierEntry createCourierEntry(Store store, CourierLocation location) {
        CourierEntry entry = new CourierEntry();
        entry.setCourierId(location.getCourierId());
        entry.setStore(store);
        entry.setTimestamp(location.getTimestamp());
        return entry;
    }

    public boolean hasRecentEntry(Store store, String courierId, LocalDateTime currentTime) {
        return courierEntryRepository.existsByStoreAndCourierIdAndTimestampAfter(
                store,
                courierId,
                currentTime.minusMinutes(minVisitIntervalMinutes)
        );
    }

    /**
     * Belirli bir kuryenin mağaza giriş kayıtlarını getirir
     *
     * @param courierId Kurye ID
     * @return Kurye giriş kayıtları listesi
     */
    public List<CourierEntryResponse> getCourierEntries(String courierId) {
        List<CourierEntry> entries = courierEntryRepository.findByCourierId(courierId);

        return entries.stream()
                .map(entry -> new CourierEntryResponse(
                        entry.getId().toString(),
                        entry.getStore().getName(),
                        entry.getTimestamp())).toList();
    }
}
