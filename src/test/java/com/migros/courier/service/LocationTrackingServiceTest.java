package com.migros.courier.service;

import com.migros.courier.exception.LocationTrackingException;
import com.migros.courier.model.CourierLocation;
import com.migros.courier.model.Store;
import com.migros.courier.repository.CourierLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.migros.courier.service.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationTrackingServiceTest {

    @Mock
    private StoreService storeService;

    @Mock
    private CourierLocationRepository courierLocationRepository;

    @InjectMocks
    private LocationTrackingService locationTrackingService;

    private List<Store> mockStores;

    @BeforeEach
    void setUp() {
        mockStores = Arrays.asList(
                new Store("Ataşehir MMM Migros", ATASEHIR_LAT, ATASEHIR_LNG),
                new Store("Novada MMM Migros", NOVADA_LAT, NOVADA_LNG)
        );
        lenient().when(storeService.getAllStores()).thenReturn(mockStores);
    }

    private CourierLocation createLocation(double lat, double lng, LocalDateTime time) {
        return new CourierLocation(COURIER_ID, lat, lng, time);
    }

    @Test
    @DisplayName("Kurye konumu kaydedildiğinde, toplam mesafe doğru hesaplanmalıdır")
    void whenLocationTracked_thenShouldCalculateTotalDistance() {
        // Given
        CourierLocation location1 = new CourierLocation(COURIER_ID, 40.9923307, 29.1244229, LocalDateTime.now());
        CourierLocation location2 = new CourierLocation(COURIER_ID, 40.986106, 29.1161293, LocalDateTime.now().plusMinutes(10));

        // When
        locationTrackingService.trackLocation(location1);
        locationTrackingService.trackLocation(location2);

        // Then
        verify(storeService, times(1)).updateTotalDistance(eq(COURIER_ID), anyDouble());
    }

    @Test
    @DisplayName("Kurye mağaza yakınında (100m) ise, mağaza girişi kaydedilmelidir")
    void whenCourierNearStore_thenShouldLogEntry() {
        // Given
        CourierLocation location = createLocation(ATASEHIR_LAT, ATASEHIR_LNG, LocalDateTime.now());
        Store store = mockStores.getFirst();

        // When
        locationTrackingService.trackLocation(location);

        // Then
        verify(storeService).logStoreEntry(store, location);
    }

    @Test
    @DisplayName("Kurye mağazadan uzakta (>100m) ise, mağaza girişi kaydedilmemelidir")
    void whenCourierFarFromStore_thenShouldNotLogEntry() {
        // Given
        CourierLocation location = new CourierLocation(COURIER_ID, 41.0082, 28.9784, LocalDateTime.now()); // Uzak bir konum

        // When
        locationTrackingService.trackLocation(location);

        // Then
        verify(storeService, never()).logStoreEntry(any(Store.class), any(CourierLocation.class));
    }

    @Test
    @DisplayName("Kurye aynı mağazaya 1 dakika içinde tekrar girdiğinde, ikinci giriş kaydedilmemelidir")
    void shouldNotTrackSameStore_WithinOneMinute() {
        // Given
        Store store = mockStores.getFirst();
        CourierLocation location = createLocation(ATASEHIR_LAT, ATASEHIR_LNG, LocalDateTime.now());

        // When
        locationTrackingService.trackLocation(location);

        // Then
        verify(storeService, times(1)).logStoreEntry(any(Store.class), any(CourierLocation.class));
        verify(storeService).hasRecentEntry(eq(store), eq(COURIER_ID), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Kurye aynı konumda kaldığında, toplam mesafe güncellenmemelidir")
    void shouldNotUpdateDistance_WhenCourierStaysSame() {
        // Given
        CourierLocation location1 = new CourierLocation(COURIER_ID, 40.9923307, 29.1244229, LocalDateTime.now());
        CourierLocation location2 = new CourierLocation(COURIER_ID, 40.9923307, 29.1244229, LocalDateTime.now().plusMinutes(5));

        // When
        locationTrackingService.trackLocation(location1);
        locationTrackingService.trackLocation(location2);

        // Then
        verify(storeService, never()).updateTotalDistance(any(), anyDouble());
    }

    @Test
    @DisplayName("Kurye farklı mağazaları ziyaret ettiğinde, her mağaza için giriş kaydı oluşturulmalıdır")
    void shouldTrackDifferentStores_WhenCourierVisitsMultipleStores() {
        // Given
        CourierLocation location1 = new CourierLocation(COURIER_ID, 40.9923307, 29.1244229, LocalDateTime.now()); // Ataşehir
        CourierLocation location2 = new CourierLocation(COURIER_ID, 40.986106, 29.1161293, LocalDateTime.now().plusHours(2)); // Novada

        // When
        locationTrackingService.trackLocation(location1);
        locationTrackingService.trackLocation(location2);

        // Then
        verify(storeService, times(2)).logStoreEntry(any(Store.class), any(CourierLocation.class));
    }

    @Test
    @DisplayName("Konum takibi sırasında hata oluştuğunda, uygun şekilde yönetilmelidir")
    void shouldHandleExceptionGracefully_WhenTrackingFails() {
        // Given
        CourierLocation location = createLocation(ATASEHIR_LAT, ATASEHIR_LNG, LocalDateTime.now());
        doThrow(new RuntimeException("DB error"))
                .when(courierLocationRepository).save(any(CourierLocation.class));

        // When & Then
        assertThrows(LocationTrackingException.class, () ->
                locationTrackingService.trackLocation(location)
        );
    }

    @Test
    @DisplayName("Geçersiz koordinat değerleri için IllegalArgumentException fırlatılmalıdır")
    void whenCoordinatesAreInvalid_thenShouldThrowException() {
        // When & Then - Geçersiz Enlem (< -90 veya > 90)
        assertThrows(IllegalArgumentException.class, () ->
            locationTrackingService.trackLocation(
                new CourierLocation(COURIER_ID, -91.0, 29.0, LocalDateTime.now())
            ), "Enlem -90 dereceden küçük olamaz"
        );

        assertThrows(IllegalArgumentException.class, () ->
            locationTrackingService.trackLocation(
                new CourierLocation(COURIER_ID, 91.0, 29.0, LocalDateTime.now())
            ), "Enlem 90 dereceden büyük olamaz"
        );

        // When & Then - Geçersiz Boylam (< -180 veya > 180)
        assertThrows(IllegalArgumentException.class, () ->
            locationTrackingService.trackLocation(
                new CourierLocation(COURIER_ID, 40.0, -181.0, LocalDateTime.now())
            ), "Boylam -180 dereceden küçük olamaz"
        );

        assertThrows(IllegalArgumentException.class, () ->
            locationTrackingService.trackLocation(
                new CourierLocation(COURIER_ID, 40.0, 181.0, LocalDateTime.now())
            ), "Boylam 180 dereceden büyük olamaz"
        );
    }
}
