package com.supertix.api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supertix.api.models.TicketModel;

public interface TicketRepository extends JpaRepository<TicketModel, Long> {
    List<TicketModel> findByOrderId(Long orderId);

    Optional<TicketModel> findByQrCode(String qrCode);
}
