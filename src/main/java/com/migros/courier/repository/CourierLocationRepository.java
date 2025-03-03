package com.migros.courier.repository;

import com.migros.courier.model.CourierLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourierLocationRepository extends JpaRepository<CourierLocation, Long> {
} 