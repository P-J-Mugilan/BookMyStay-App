package com.bookmystay.app.controller;

import com.bookmystay.app.dto.reponse.ApiResponse;
import com.bookmystay.app.dto.reponse.BookingResponse;
import com.bookmystay.app.dto.request.AddServiceRequest;
import com.bookmystay.app.entity.HotelService;
import com.bookmystay.app.service.HotelServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HotelServiceController {

    private final HotelServiceService hotelServiceService;

    @GetMapping("/services")
    public ResponseEntity<ApiResponse<List<HotelService>>> getAllServices() {
        List<HotelService> response = hotelServiceService.getAllServices();
        return ResponseEntity.ok(
                ApiResponse.<List<HotelService>>builder()
                        .success(true)
                        .message("Add-on services retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/bookings/{bookingId}/services")
    public ResponseEntity<ApiResponse<BookingResponse>> addServiceToBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody AddServiceRequest request) {
        BookingResponse response = hotelServiceService.addServiceToBooking(bookingId, request);
        return ResponseEntity.ok(
                ApiResponse.<BookingResponse>builder()
                        .success(true)
                        .message("Service added to booking successfully.")
                        .data(response)
                        .build()
        );
    }

}
