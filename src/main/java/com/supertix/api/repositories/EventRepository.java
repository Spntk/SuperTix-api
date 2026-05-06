package com.supertix.api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supertix.api.enums.EventStatus;
import com.supertix.api.models.EventModel;

public interface EventRepository extends JpaRepository<EventModel, Long> {
    List<EventModel> findByVenueId(Long venueId);

    List<EventModel> findByStatus(EventStatus status);
}
