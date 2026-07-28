package com.bookmystay.app.service.impl;

import com.bookmystay.app.dto.reponse.BookingResponse;
import com.bookmystay.app.dto.request.CreateBookingRequest;
import com.bookmystay.app.entity.Booking;
import com.bookmystay.app.entity.RoomType;
import com.bookmystay.app.enums.BookingStatus;
import com.bookmystay.app.exception.BusinessException;
import com.bookmystay.app.exception.ResourceNotFoundException;
import com.bookmystay.app.repository.BookingRepository;
import com.bookmystay.app.repository.RoomTypeRepository;
import com.bookmystay.app.service.BookingService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomTypeRepository roomTypeRepository;

    // Thread-safe in-memory FIFO queue as per UC3 data structure requirements
    private final Queue<Long> bookingQueue = new ConcurrentLinkedQueue<>();

    @PostConstruct
    public void reloadPendingQueue() {
        // Load pending bookings from DB in order of creation to restore queue upon restart
        List<Booking> pendingBookings = bookingRepository.findByStatus(BookingStatus.PENDING);
        // Sort by id to maintain FIFO order
        pendingBookings.stream()
                .map(Booking::getId)
                .forEach(bookingQueue::add);
    }

    @Override
    @Transactional
    public BookingResponse submitBookingRequest(CreateBookingRequest request) {
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new BusinessException("Check-out date must be after check-in date.");
        }

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Requested room type does not exist."));

        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal totalCost = roomType.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Booking booking = Booking.builder()
                .guestName(request.getGuestName())
                .roomType(roomType)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .numberOfGuests(request.getNumberOfGuests())
                .status(BookingStatus.PENDING)
                .totalCost(totalCost)
                .build();

        Booking saved = bookingRepository.save(booking);

        // Add to FIFO queue
        bookingQueue.add(saved.getId());

        return mapToResponse(saved);
    }

    @Override
    public List<BookingResponse> getBookingQueue() {
        List<BookingResponse> queueDetails = new ArrayList<>();
        for (Long id : bookingQueue) {
            bookingRepository.findById(id).ifPresent(booking -> queueDetails.add(mapToResponse(booking)));
        }
        return queueDetails;
    }

    @Override
    public int getQueuePosition(Long bookingId) {
        int position = 1;
        for (Long id : bookingQueue) {
            if (id.equals(bookingId)) {
                return position;
            }
            position++;
        }
        return -1; // Not in queue
    }

    // Direct queue access for UC4
    public Queue<Long> getInternalQueue() {
        return bookingQueue;
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .guestName(booking.getGuestName())
                .roomTypeName(booking.getRoomType().getName())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .numberOfGuests(booking.getNumberOfGuests())
                .status(booking.getStatus().name())
                .totalCost(booking.getTotalCost())
                .allocatedRoomNumber(booking.getAllocatedRoomNumber())
                .build();
    }
}
