package com.migros.courier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CourierEntryResponse {
    private String storeId;
    private String storeName;
    private LocalDateTime entryTime;
}
