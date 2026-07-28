package com.bookmystay.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeRequest {

    @NotBlank(message = "Room type name cannot be blank")
    private String name;

    @NotNull(message = "Available rooms count is required")
    @PositiveOrZero(message = "Available rooms cannot be negative")
    private Integer availableRooms;

    @NotNull(message = "Price per night is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal pricePerNight;

    private String description;

}
