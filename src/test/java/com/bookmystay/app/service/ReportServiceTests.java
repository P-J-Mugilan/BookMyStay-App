package com.bookmystay.app.service;

import com.bookmystay.app.dto.reponse.BookingResponse;
import com.bookmystay.app.dto.reponse.RoomTypeResponse;
import com.bookmystay.app.dto.request.AddServiceRequest;
import com.bookmystay.app.dto.request.CreateBookingRequest;
import com.bookmystay.app.entity.HotelService;
import com.bookmystay.app.enums.BookingStatus;
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
class ReportServiceTests {

    @Autowired
    private ReportService reportService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private HotelServiceService hotelServiceService;

    private Long singleRoomTypeId;
    private Long doubleRoomTypeId;
    private Long breakfastServiceId;

    @BeforeEach
    void setUp() {
        List<RoomTypeResponse> roomTypes = roomTypeService.getAllRoomTypes();
        singleRoomTypeId = roomTypes.stream()
                .filter(rt -> rt.getName().equalsIgnoreCase("Single"))
                .findFirst()
                .orElseThrow()
                .getId();

        doubleRoomTypeId = roomTypes.stream()
                .filter(rt -> rt.getName().equalsIgnoreCase("Double"))
                .findFirst()
                .orElseThrow()
                .getId();

        List<HotelService> services = hotelServiceService.getAllServices();
        breakfastServiceId = services.stream()
                .filter(s -> s.getName().equalsIgnoreCase("Breakfast"))
                .findFirst()
                .orElseThrow()
                .getId();

        if (bookingService instanceof com.bookmystay.app.service.impl.BookingServiceImpl) {
            ((com.bookmystay.app.service.impl.BookingServiceImpl) bookingService).getInternalQueue().clear();
            ((com.bookmystay.app.service.impl.BookingServiceImpl) bookingService).getBookedRoomIds().clear();
            ((com.bookmystay.app.service.impl.BookingServiceImpl) bookingService).getAllocatedRooms().clear();
        }
    }

    @Test
    void testReportingAndCancellationFlow() {
        // 1. Submit Alice's request (Single, 2 nights = $200) and Bob's request (Double, 1 night = $200)
        CreateBookingRequest aliceRequest = new CreateBookingRequest(
                "Alice",
                singleRoomTypeId,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                1
        );
        CreateBookingRequest bobRequest = new CreateBookingRequest(
                "Bob",
                doubleRoomTypeId,
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(3),
                2
        );

        BookingResponse alice = bookingService.submitBookingRequest(aliceRequest);
        BookingResponse bob = bookingService.submitBookingRequest(bobRequest);

        // 2. Confirm both bookings
        bookingService.confirmNextBooking(); // Confirm Alice
        bookingService.confirmNextBooking(); // Confirm Bob

        // 3. Add Breakfast ($15) to Alice
        hotelServiceService.addServiceToBooking(alice.getId(), new AddServiceRequest(breakfastServiceId));

        // 4. Validate Total Revenue ($215 + $200 = $415)
        BigDecimal revenue = reportService.getTotalRevenue();
        assertEquals(0, BigDecimal.valueOf(415.00).compareTo(revenue));

        // 5. Validate Occupancy Rate (2 rooms out of 17 total = ~11.76%)
        double occupancy = reportService.getOccupancyRate();
        assertEquals((2.0 / 17.0) * 100.0, occupancy, 0.01);

        // 6. Search bookings by guest
        List<BookingResponse> aliceHistory = reportService.getBookingsByGuest("Alice");
        assertEquals(1, aliceHistory.size());
        assertEquals("CONFIRMED", aliceHistory.get(0).getStatus());

        // 7. Cancel Bob's booking
        BookingResponse cancelledBob = bookingService.cancelBooking(bob.getId());
        assertEquals("CANCELLED", cancelledBob.getStatus());

        // 8. Recalculate Occupancy and Revenue
        // Bob's $200 is gone, revenue should be Alice's $215
        BigDecimal updatedRevenue = reportService.getTotalRevenue();
        assertEquals(0, BigDecimal.valueOf(215.00).compareTo(updatedRevenue));

        // Bob's room is freed, only 1 occupied room now
        double updatedOccupancy = reportService.getOccupancyRate();
        assertEquals((1.0 / 17.0) * 100.0, updatedOccupancy, 0.01);

        // Inventory for Double should be restored to 5 (from 4)
        RoomTypeResponse doubleRoomType = roomTypeService.getRoomTypeById(doubleRoomTypeId);
        assertEquals(5, doubleRoomType.getAvailableRooms());
    }
}
