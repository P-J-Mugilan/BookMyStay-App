package com.bookmystay.app.service.impl;

import com.bookmystay.app.dto.reponse.BookingResponse;
import com.bookmystay.app.dto.request.CreateBookingRequest;
import com.bookmystay.app.entity.Booking;
import com.bookmystay.app.entity.Room;
import com.bookmystay.app.entity.RoomType;
import com.bookmystay.app.enums.BookingStatus;
import com.bookmystay.app.enums.RoomStatus;
import com.bookmystay.app.exception.BusinessException;
import com.bookmystay.app.exception.ResourceNotFoundException;
import com.bookmystay.app.repository.BookingRepository;
import com.bookmystay.app.repository.BookingAddonRepository;
import com.bookmystay.app.repository.RoomRepository;
import com.bookmystay.app.repository.RoomTypeRepository;
import com.bookmystay.app.service.BookingService;
import com.bookmystay.app.service.RoomTypeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeService roomTypeService;
    private final BookingAddonRepository bookingAddonRepository;

    // Thread-safe in-memory FIFO queue as per UC3 data structure requirements
    private final Queue<Long> bookingQueue = new ConcurrentLinkedQueue<>();

    // Thread-safe in-memory collections for UC4 tracking as per requirements
    private final Set<String> bookedRoomIds = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, Set<String>> allocatedRooms = new ConcurrentHashMap<>();

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

    @Override
    @Transactional
    public synchronized BookingResponse confirmNextBooking() {
        Long bookingId = bookingQueue.poll();
        if (bookingId == null) {
            throw new BusinessException("No bookings currently waiting in the pipeline.");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        RoomType roomType = booking.getRoomType();

        // 1. Fetch available physical rooms of this type
        List<Room> availableRoomsList = roomRepository.findByRoomTypeAndStatus(roomType, RoomStatus.AVAILABLE);

        if (availableRoomsList.isEmpty()) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            throw new BusinessException("No rooms available for the selected room type: " + roomType.getName());
        }

        // 2. Allocate the first available room
        Room roomToAllocate = availableRoomsList.get(0);
        roomToAllocate.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(roomToAllocate);

        // 3. Update booking status
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setAllocatedRoomNumber(roomToAllocate.getRoomNumber());
        Booking confirmedBooking = bookingRepository.save(booking);

        // 4. Decrement available room count in inventory
        int newAvailability = roomType.getAvailableRooms() - 1;
        if (newAvailability < 0) {
            throw new BusinessException("Room inventory inconsistency detected.");
        }
        roomType.setAvailableRooms(newAvailability);
        roomTypeRepository.save(roomType);

        // 5. Update O(1) in-memory data structures
        bookedRoomIds.add(roomToAllocate.getRoomNumber());
        allocatedRooms.computeIfAbsent(roomType.getName().toLowerCase(), k -> ConcurrentHashMap.newKeySet())
                .add(roomToAllocate.getRoomNumber());

        if (roomTypeService instanceof RoomTypeServiceImpl) {
            ((RoomTypeServiceImpl) roomTypeService).updateCachedInventory(roomType.getName(), newAvailability);
        }

        return mapToResponse(confirmedBooking);
    }

    public Set<String> getBookedRoomIds() {
        return bookedRoomIds;
    }

    public ConcurrentHashMap<String, Set<String>> getAllocatedRooms() {
        return allocatedRooms;
    }

    private BookingResponse mapToResponse(Booking booking) {
        List<String> serviceNames = new ArrayList<>();
        if (bookingAddonRepository != null) {
            serviceNames = bookingAddonRepository.findByBookingId(booking.getId()).stream()
                    .map(addon -> addon.getHotelService().getName())
                    .collect(Collectors.toList());
        }

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
                .services(serviceNames)
                .build();
    }
}
