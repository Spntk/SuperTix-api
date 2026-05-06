package com.supertix.api.dtos.ticket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TicketResponse {

    private Long id;
    private String eventTitle;
    private String eventStartDate;
    private String venueName;
    private String zoneName;
    private String seatLabel;
    private String qrCode;
    private Boolean isScanned;
    private String createdAt;
}
