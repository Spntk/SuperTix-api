package com.supertix.api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.supertix.api.dtos.ticket.AdminTicketResponse;
import com.supertix.api.dtos.ticket.TicketResponse;
import com.supertix.api.models.OrderModel;
import com.supertix.api.models.TicketModel;
import com.supertix.api.repositories.OrderRepository;
import com.supertix.api.repositories.TicketRepository;

import jakarta.transaction.Transactional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final OrderRepository orderRepository;

    public TicketService(TicketRepository ticketRepository, OrderRepository orderRepository) {
        this.ticketRepository = ticketRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public List<TicketResponse> getMyTickets(Long userId) {
        List<OrderModel> orders = orderRepository.findByUserId(userId);

        return orders.stream()
                .flatMap(order -> ticketRepository.findByOrderId(order.getId()).stream())
                .map(ticket -> new TicketResponse(
                        ticket.getId(),
                        ticket.getOrder().getEvent().getTitle(),
                        ticket.getOrder().getEvent().getStartDate().toString(),
                        ticket.getOrder().getEvent().getVenue().getName(),
                        ticket.getSeat().getZone().getName(),
                        ticket.getSeat().getRowLabel() + ticket.getSeat().getSeatNumber(),
                        ticket.getQrCode(),
                        ticket.getIsScanned(),
                        ticket.getCreatedAt().toString()))
                .toList();
    }

    @Transactional
    public List<AdminTicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(ticket -> new AdminTicketResponse(
                        ticket.getId(),
                        ticket.getOrder().getId(),
                        ticket.getOrder().getStatus().name(),
                        ticket.getOrder().getUser().getId(),
                        ticket.getOrder().getUser().getName(),
                        ticket.getOrder().getUser().getEmail(),
                        ticket.getOrder().getEvent().getTitle(),
                        ticket.getOrder().getEvent().getStartDate().toString(),
                        ticket.getOrder().getEvent().getVenue().getName(),
                        ticket.getSeat().getZone().getName(),
                        ticket.getSeat().getZone().getType() != null
                                ? ticket.getSeat().getZone().getType().name()
                                : null,
                        ticket.getSeat().getRowLabel() + ticket.getSeat().getSeatNumber(),
                        ticket.getSeat().getZone().getPrice(),
                        ticket.getQrCode(),
                        ticket.getIsScanned(),
                        ticket.getCreatedAt().toString()))
                .toList();
    }

    public Map<String, String> scanTicket(Long ticketId) {
        TicketModel ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket not found"));

        if (ticket.getIsScanned()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket already scanned");
        }

        ticket.setIsScanned(true);
        ticketRepository.save(ticket);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Scan QR code successful");
        return response;
    }
}
