package com.supertix.api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supertix.api.models.ZoneModel;

public interface ZoneRepository extends JpaRepository<ZoneModel, Long> {
    List<ZoneModel> findByEventId(Long eventId);
}
