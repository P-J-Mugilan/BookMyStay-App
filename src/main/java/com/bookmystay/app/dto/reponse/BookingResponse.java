package com.bookmystay.app.dto.reponse;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;
    private String guestName;
    private String roomTypeName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfGuests;
    private String status;
    private BigDecimal totalCost;
    private String allocatedRoomNumber;

}
