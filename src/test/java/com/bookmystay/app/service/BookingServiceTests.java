package com.bookmystay.app.service;

import com.bookmystay.app.dto.reponse.BookingResponse;
import com.bookmystay.app.dto.reponse.RoomTypeResponse;
import com.bookmystay.app.dto.request.CreateBookingRequest;
import com.bookmystay.app.exception.BusinessException;
import com.bookmystay.app.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookingServiceTests {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomTypeService roomTypeService;

    private Long singleRoomTypeId;
    private Long doubleRoomTypeId;

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

        // Clear queue by reinitializing (transactional rollbacks clear DB, but we want to make sure in-memory queue matches)
        if (bookingService instanceof com.bookmystay.app.service.impl.BookingServiceImpl) {
            ((com.bookmystay.app.service.impl.BookingServiceImpl) bookingService).getInternalQueue().clear();
        }
    }

    @Test
    void testSubmitBookingRequestQueuedSuccessfully() {
        CreateBookingRequest request = new CreateBookingRequest(
                "Alice",
                singleRoomTypeId,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                1
        );

        BookingResponse response = bookingService.submitBookingRequest(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Alice", response.getGuestName());
        assertEquals("Single", response.getRoomTypeName());
        assertEquals("PENDING", response.getStatus());
        assertEquals(1, bookingService.getQueuePosition(response.getId()));
    }

    @Test
    void testBookingFifoOrdering() {
        CreateBookingRequest request1 = new CreateBookingRequest(
                "Alice",
                singleRoomTypeId,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                1
        );
        CreateBookingRequest request2 = new CreateBookingRequest(
                "Bob",
                doubleRoomTypeId,
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(4),
                2
        );

        BookingResponse response1 = bookingService.submitBookingRequest(request1);
        BookingResponse response2 = bookingService.submitBookingRequest(request2);

        List<BookingResponse> queue = bookingService.getBookingQueue();
        assertEquals(2, queue.size());
        assertEquals(response1.getId(), queue.get(0).getId());
        assertEquals(response2.getId(), queue.get(1).getId());

        assertEquals(1, bookingService.getQueuePosition(response1.getId()));
        assertEquals(2, bookingService.getQueuePosition(response2.getId()));
    }

    @Test
    void testInvalidDatesThrowsException() {
        CreateBookingRequest request = new CreateBookingRequest(
                "Alice",
                singleRoomTypeId,
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(1), // checkout before checkin
                1
        );

        assertThrows(BusinessException.class, () -> bookingService.submitBookingRequest(request));
    }

    @Test
    void testInvalidRoomTypeThrowsException() {
        CreateBookingRequest request = new CreateBookingRequest(
                "Alice",
                999L, // invalid ID
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                1
        );

        assertThrows(ResourceNotFoundException.class, () -> bookingService.submitBookingRequest(request));
    }
}
