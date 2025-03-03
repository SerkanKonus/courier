package com.migros.courier.repository;

import com.migros.courier.model.CourierEntry;
import com.migros.courier.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourierEntryRepository extends JpaRepository<CourierEntry, Long> {
    boolean existsByStoreAndCourierIdAndTimestampAfter(Store store, String courierId, LocalDateTime time);

    List<CourierEntry> findByCourierId(String courierId);
}
