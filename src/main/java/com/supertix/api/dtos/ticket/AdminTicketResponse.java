package com.supertix.api.dtos.ticket;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminTicketResponse {

    private Long id;
    private Long orderId;
    private String orderStatus;
    private Long userId;
    private String userName;
    private String userEmail;
    private String eventTitle;
    private String eventStartDate;
    private String venueName;
    private String zoneName;
    private String zoneType;
    private String seatLabel;
    private BigDecimal price;
    private String qrCode;
    private Boolean isScanned;
    private String createdAt;
}
