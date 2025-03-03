package com.migros.courier.model;

import com.migros.courier.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class CourierEntry extends BaseEntity {

    @Column(nullable = false)
    private String courierId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

}
