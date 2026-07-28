package com.bookmystay.app.controller;

import com.bookmystay.app.dto.reponse.ApiResponse;
import com.bookmystay.app.dto.reponse.RoomTypeResponse;
import com.bookmystay.app.dto.request.RoomTypeRequest;
import com.bookmystay.app.service.RoomTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoomTypeResponse>> createRoomType(
            @Valid @RequestBody RoomTypeRequest request) {
        RoomTypeResponse response = roomTypeService.createRoomType(request);
        return new ResponseEntity<>(
                ApiResponse.<RoomTypeResponse>builder()
                        .success(true)
                        .message("Room type created successfully.")
                        .data(response)
                        .build(),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomTypeResponse>>> getAllRoomTypes() {
        List<RoomTypeResponse> response = roomTypeService.getAllRoomTypes();
        return ResponseEntity.ok(
                ApiResponse.<List<RoomTypeResponse>>builder()
                        .success(true)
                        .message("Room types retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomTypeResponse>> getRoomTypeById(@PathVariable Long id) {
        RoomTypeResponse response = roomTypeService.getRoomTypeById(id);
        return ResponseEntity.ok(
                ApiResponse.<RoomTypeResponse>builder()
                        .success(true)
                        .message("Room type retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomTypeResponse>> updateRoomType(
            @PathVariable Long id,
            @Valid @RequestBody RoomTypeRequest request) {
        RoomTypeResponse response = roomTypeService.updateRoomType(id, request);
        return ResponseEntity.ok(
                ApiResponse.<RoomTypeResponse>builder()
                        .success(true)
                        .message("Room type updated successfully.")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoomType(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Room type deleted successfully.")
                        .build()
        );
    }

}
