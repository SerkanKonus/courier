package com.migros.courier.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.migros.courier.exception.StoreEntryException;
import com.migros.courier.model.CourierEntry;
import com.migros.courier.model.CourierLocation;
import com.migros.courier.model.Store;
import com.migros.courier.repository.CourierEntryRepository;
import com.migros.courier.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.migros.courier.service.TestConstants.COURIER_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private CourierEntryRepository courierEntryRepository;
    @Mock
    private ObjectMapper objectMapper;

    private StoreService storeService;

    @BeforeEach
    void setUp() {
        storeService = new StoreService(storeRepository, courierEntryRepository, objectMapper);
    }

    @Test
    @DisplayName("Tüm Migros mağazaları başarıyla listelenmelidir")
    void whenGetAllStores_thenReturnAllMigrosStores() {
        // Given
        List<Store> mockStores = Arrays.asList(
                new Store("Ataşehir MMM Migros", 40.9923307, 29.1244229),
                new Store("Novada MMM Migros", 40.986106, 29.1161293)
        );
        when(storeRepository.findAll()).thenReturn(mockStores);

        // When
        List<Store> stores = storeService.getAllStores();

        // Then
        assertNotNull(stores);
        assertFalse(stores.isEmpty());
        assertTrue(stores.stream().allMatch(store -> store.getName().contains("Migros")));
        verify(storeRepository).findAll();
    }

    @Test
    @DisplayName("Kurye hareket ettikçe, toplam mesafe birikimli olarak toplanmalı ve doğru şekilde güncellenmelidir")
    void whenUpdateTotalDistance_thenShouldAccumulateDistance() {
        // When
        storeService.updateTotalDistance(COURIER_ID, 100.0); // İlk hareket
        storeService.updateTotalDistance(COURIER_ID, 50.0);  // İkinci hareket

        // Then
        assertEquals(150.0, storeService.getTotalTravelDistance(COURIER_ID));
    }

    @Test
    @DisplayName("Negatif mesafe değeri girildiğinde, toplam mesafe güncellenmemelidir")
    void whenNegativeDistance_thenShouldNotUpdate() {
        // When
        storeService.updateTotalDistance(COURIER_ID, 100.0);
        storeService.updateTotalDistance(COURIER_ID, -50.0);  // Negatif değer

        // Then
        assertEquals(100.0, storeService.getTotalTravelDistance(COURIER_ID));
    }

    @Test
    @DisplayName("Kurye mağaza ziyaretlerinde 1 dakika kuralı doğru şekilde uygulanmalıdır")
    void whenCourierVisitsStore_thenShouldApplyOneMinuteRule() {
        // Given
        Store store = new Store("Test Migros", 40.0, 29.0);
        LocalDateTime currentTime = LocalDateTime.now();
        
        // When - İlk ziyaret (kayıt yok)
        when(courierEntryRepository.existsByStoreAndCourierIdAndTimestampAfter(
                eq(store), eq(COURIER_ID), any(LocalDateTime.class)
        )).thenReturn(false);
        boolean firstVisit = storeService.hasRecentEntry(store, COURIER_ID, currentTime);
        
        // Then
        assertFalse(firstVisit, "İlk ziyaret kaydedilmelidir");
        
        // When - 30 saniye sonra tekrar ziyaret (1 dakika dolmadı)
        when(courierEntryRepository.existsByStoreAndCourierIdAndTimestampAfter(
                eq(store), eq(COURIER_ID), any(LocalDateTime.class)
        )).thenReturn(true);
        boolean secondVisit = storeService.hasRecentEntry(store, COURIER_ID, currentTime.plusSeconds(30));
        
        // Then
        assertTrue(secondVisit, "1 dakika dolmadan yapılan ziyaret engellenmeli");
    }

    @Test
    @DisplayName("Kurye mağaza girişi yapıldığında, giriş kaydı veritabanına başarıyla kaydedilmelidir")
    void whenLogStoreEntry_thenShouldSaveEntry() {
        // Given
        Store store = new Store("Test Migros", 40.0, 29.0);
        CourierLocation location = new CourierLocation(COURIER_ID, 40.0, 29.0, LocalDateTime.now());

        // When
        storeService.logStoreEntry(store, location);

        // Then
        verify(courierEntryRepository).save(any(CourierEntry.class));
    }

    @Test
    @DisplayName("Mağaza giriş kaydı oluşturulurken hata olursa, StoreEntryException fırlatılmalıdır")
    void whenEntryLogFails_thenShouldThrowException() {
        // Given
        Store store = new Store("Test Migros", 40.0, 29.0);
        CourierLocation location = new CourierLocation(COURIER_ID, 40.0, 29.0, LocalDateTime.now());
        when(courierEntryRepository.save(any(CourierEntry.class)))
                .thenThrow(new RuntimeException("DB hatası"));

        // When & Then
        assertThrows(StoreEntryException.class, () ->
                storeService.logStoreEntry(store, location)
        );
    }

    @Test
    @DisplayName("Kurye ID null veya boş olduğunda IllegalArgumentException fırlatılmalıdır")
    void whenCourierIdIsInvalid_thenShouldThrowException() {
        // When & Then - Null ID
        assertThrows(IllegalArgumentException.class, () ->
            storeService.updateTotalDistance(null, 100.0),
            "Null kurye ID kabul edilmemeli"
        );

        // When & Then - Boş ID
        assertThrows(IllegalArgumentException.class, () ->
            storeService.updateTotalDistance("", 100.0),
            "Boş kurye ID kabul edilmemeli"
        );

        // When & Then - Sadece boşluk içeren ID
        assertThrows(IllegalArgumentException.class, () ->
            storeService.updateTotalDistance("   ", 100.0),
            "Boşluk karakterlerinden oluşan kurye ID kabul edilmemeli"
        );
    }

}
