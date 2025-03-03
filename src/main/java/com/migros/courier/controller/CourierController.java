package com.migros.courier.controller;

import com.migros.courier.dto.CourierEntryResponse;
import com.migros.courier.model.CourierLocation;
import com.migros.courier.service.LocationTrackingService;
import com.migros.courier.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courier")
@RequiredArgsConstructor
@Tag(name = "Courier API", description = "Kurye takip ve mesafe hesaplama API'leri")
public class CourierController {
    private final LocationTrackingService locationTrackingService;
    private final StoreService storeService;

    @PostMapping("/location")
    @Operation(summary = "Kurye lokasyonu kaydet")
    public ResponseEntity<Void> trackLocation(@Valid @RequestBody CourierLocation location) {
        locationTrackingService.trackLocation(location);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{courierId}/total-distance")
    @Operation(summary = "Kurye toplam mesafe sorgula")
    public ResponseEntity<Double> getTotalDistance(@PathVariable String courierId) {
        return ResponseEntity.ok(storeService.getTotalTravelDistance(courierId));
    }

    @GetMapping("/{courierId}/entries")
    public ResponseEntity<List<CourierEntryResponse>> getCourierEntries(@PathVariable String courierId) {
        List<CourierEntryResponse> entries = storeService.getCourierEntries(courierId);
        return ResponseEntity.ok(entries);
    }
}
