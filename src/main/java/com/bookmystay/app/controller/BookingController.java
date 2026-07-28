package com.bookmystay.app.controller;

import com.bookmystay.app.dto.reponse.ApiResponse;
import com.bookmystay.app.dto.reponse.BookingResponse;
import com.bookmystay.app.dto.request.CreateBookingRequest;
import com.bookmystay.app.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> submitBookingRequest(
            @Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.submitBookingRequest(request);
        return new ResponseEntity<>(
                ApiResponse.<BookingResponse>builder()
                        .success(true)
                        .message("Booking request queued successfully.")
                        .data(response)
                        .build(),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingQueue() {
        List<BookingResponse> response = bookingService.getBookingQueue();
        return ResponseEntity.ok(
                ApiResponse.<List<BookingResponse>>builder()
                        .success(true)
                        .message("Booking queue retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/queue/{id}/position")
    public ResponseEntity<ApiResponse<Integer>> getQueuePosition(@PathVariable Long id) {
        int position = bookingService.getQueuePosition(id);
        if (position == -1) {
            return ResponseEntity.ok(
                    ApiResponse.<Integer>builder()
                            .success(false)
                            .message("Booking is not in the pending queue.")
                            .data(-1)
                            .build()
            );
        }
        return ResponseEntity.ok(
                ApiResponse.<Integer>builder()
                        .success(true)
                        .message("Queue position retrieved successfully.")
                        .data(position)
                        .build()
        );
    }

}
