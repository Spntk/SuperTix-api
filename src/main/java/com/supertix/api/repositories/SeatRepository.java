package com.supertix.api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supertix.api.models.SeatModel;

public interface SeatRepository extends JpaRepository<SeatModel, Long> {
    List<SeatModel> findByZoneId(Long zoneId);

    List<SeatModel> findByZoneEventId(Long eventId);

    long countByZoneId(Long zoneId);

    boolean existsByZoneIdAndRowLabelAndSeatNumber(Long zoneId, String rowLabel, Integer seatNumber);
}
