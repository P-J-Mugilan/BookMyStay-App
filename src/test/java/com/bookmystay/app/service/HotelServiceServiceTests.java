package com.bookmystay.app.service;

import com.bookmystay.app.dto.reponse.BookingResponse;
import com.bookmystay.app.dto.reponse.RoomTypeResponse;
import com.bookmystay.app.dto.request.AddServiceRequest;
import com.bookmystay.app.dto.request.CreateBookingRequest;
import com.bookmystay.app.entity.HotelService;
import com.bookmystay.app.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class HotelServiceServiceTests {

    @Autowired
    private HotelServiceService hotelServiceService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomTypeService roomTypeService;

    private Long singleRoomTypeId;
    private Long breakfastServiceId;
    private Long spaServiceId;

    @BeforeEach
    void setUp() {
        List<RoomTypeResponse> roomTypes = roomTypeService.getAllRoomTypes();
        singleRoomTypeId = roomTypes.stream()
                .filter(rt -> rt.getName().equalsIgnoreCase("Single"))
                .findFirst()
                .orElseThrow()
                .getId();

        List<HotelService> services = hotelServiceService.getAllServices();
        breakfastServiceId = services.stream()
                .filter(s -> s.getName().equalsIgnoreCase("Breakfast"))
                .findFirst()
                .orElseThrow()
                .getId();

        spaServiceId = services.stream()
                .filter(s -> s.getName().equalsIgnoreCase("Spa"))
                .findFirst()
                .orElseThrow()
                .getId();

        // Clear queue
        if (bookingService instanceof com.bookmystay.app.service.impl.BookingServiceImpl) {
            ((com.bookmystay.app.service.impl.BookingServiceImpl) bookingService).getInternalQueue().clear();
        }
    }

    @Test
    void testAddServiceToBookingSuccess() {
        // 1. Submit a booking request (Single room: $100.00/night * 2 nights = $200.00)
        CreateBookingRequest createRequest = new CreateBookingRequest(
                "Alice",
                singleRoomTypeId,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                1
        );
        BookingResponse queued = bookingService.submitBookingRequest(createRequest);
        BigDecimal initialCost = queued.getTotalCost();
        assertEquals(0, BigDecimal.valueOf(200.00).compareTo(initialCost));

        // 2. Confirm the booking to allocate room
        bookingService.confirmNextBooking();

        // 3. Add Breakfast ($15.00)
        AddServiceRequest addBreakfast = new AddServiceRequest(breakfastServiceId);
        BookingResponse withBreakfast = hotelServiceService.addServiceToBooking(queued.getId(), addBreakfast);

        assertNotNull(withBreakfast);
        assertEquals(1, withBreakfast.getServices().size());
        assertTrue(withBreakfast.getServices().contains("Breakfast"));
        // Total cost should be $215.00
        assertEquals(0, BigDecimal.valueOf(215.00).compareTo(withBreakfast.getTotalCost()));

        // 4. Add Spa ($50.00)
        AddServiceRequest addSpa = new AddServiceRequest(spaServiceId);
        BookingResponse withSpa = hotelServiceService.addServiceToBooking(queued.getId(), addSpa);

        assertNotNull(withSpa);
        assertEquals(2, withSpa.getServices().size());
        assertTrue(withSpa.getServices().contains("Spa"));
        // Total cost should be $265.00
        assertEquals(0, BigDecimal.valueOf(265.00).compareTo(withSpa.getTotalCost()));
    }

    @Test
    void testAddServiceToInvalidBookingThrowsException() {
        AddServiceRequest request = new AddServiceRequest(breakfastServiceId);
        assertThrows(ResourceNotFoundException.class, () -> hotelServiceService.addServiceToBooking(999L, request));
    }

    @Test
    void testAddInvalidServiceThrowsException() {
        // Submit a booking request
        CreateBookingRequest createRequest = new CreateBookingRequest(
                "Alice",
                singleRoomTypeId,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                1
        );
        BookingResponse queued = bookingService.submitBookingRequest(createRequest);
        AddServiceRequest request = new AddServiceRequest(999L);

        assertThrows(ResourceNotFoundException.class, () -> hotelServiceService.addServiceToBooking(queued.getId(), request));
    }
}
