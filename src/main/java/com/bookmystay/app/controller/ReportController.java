package com.bookmystay.app.controller;

import com.bookmystay.app.dto.reponse.ApiResponse;
import com.bookmystay.app.dto.reponse.BookingResponse;
import com.bookmystay.app.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingHistory() {
        List<BookingResponse> response = reportService.getBookingHistory();
        return ResponseEntity.ok(
                ApiResponse.<List<BookingResponse>>builder()
                        .success(true)
                        .message("Booking history retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/occupancy")
    public ResponseEntity<ApiResponse<Double>> getOccupancyRate() {
        double response = reportService.getOccupancyRate();
        return ResponseEntity.ok(
                ApiResponse.<Double>builder()
                        .success(true)
                        .message("Occupancy rate retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalRevenue() {
        BigDecimal response = reportService.getTotalRevenue();
        return ResponseEntity.ok(
                ApiResponse.<BigDecimal>builder()
                        .success(true)
                        .message("Total revenue retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsByGuest(
            @RequestParam String guestName) {
        List<BookingResponse> response = reportService.getBookingsByGuest(guestName);
        return ResponseEntity.ok(
                ApiResponse.<List<BookingResponse>>builder()
                        .success(true)
                        .message("Bookings search completed successfully.")
                        .data(response)
                        .build()
        );
    }

}
