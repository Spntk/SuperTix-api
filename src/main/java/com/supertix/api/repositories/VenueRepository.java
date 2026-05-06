package com.supertix.api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supertix.api.models.VenueModel;

public interface VenueRepository extends JpaRepository<VenueModel, Long> {
    boolean existsByName(String name);

    List<VenueModel> findAllByOrderByIdAsc();
}
