package com.bookmystay.app.dto.reponse;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomTypeResponse {

    private Long id;
    private String name;
    private Integer availableRooms;
    private BigDecimal pricePerNight;
    private String description;

}
