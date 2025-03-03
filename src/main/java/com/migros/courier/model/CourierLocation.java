package com.migros.courier.model;

import com.migros.courier.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourierLocation extends BaseEntity {

    @NotBlank(message = "Kurye ID boş olamaz")
    @Column(nullable = false)
    private String courierId;

    @NotNull(message = "Enlem değeri boş olamaz")
    @Column(nullable = false)
    private double lat;

    @NotNull(message = "Boylam değeri boş olamaz")
    @Column(nullable = false)
    private double lng;

    @NotNull(message = "Zaman bilgisi boş olamaz")
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
