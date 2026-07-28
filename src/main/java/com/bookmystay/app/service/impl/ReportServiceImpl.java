package com.bookmystay.app.service.impl;

import com.bookmystay.app.dto.reponse.BookingResponse;
import com.bookmystay.app.entity.Booking;
import com.bookmystay.app.entity.Room;
import com.bookmystay.app.entity.RoomType;
import com.bookmystay.app.enums.BookingStatus;
import com.bookmystay.app.enums.RoomStatus;
import com.bookmystay.app.repository.BookingAddonRepository;
import com.bookmystay.app.repository.BookingRepository;
import com.bookmystay.app.repository.RoomRepository;
import com.bookmystay.app.service.ReportService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final BookingAddonRepository bookingAddonRepository;

    // In-memory list to track full booking history as per UC6 requirements
    private final List<BookingResponse> bookingHistory = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void loadHistory() {
        refreshHistory();
    }

    public void refreshHistory() {
        List<Booking> all = bookingRepository.findAll();
        bookingHistory.clear();
        for (Booking b : all) {
            bookingHistory.add(mapToResponse(b));
        }
    }

    @Override
    public List<BookingResponse> getBookingHistory() {
        refreshHistory(); // Ensure fresh state
        return bookingHistory;
    }

    @Override
    public double getOccupancyRate() {
        List<Room> rooms = roomRepository.findAll();
        if (rooms.isEmpty()) {
            return 0.0;
        }
        long occupiedCount = rooms.stream()
                .filter(r -> r.getStatus() == RoomStatus.OCCUPIED)
                .count();
        return ((double) occupiedCount / rooms.size()) * 100.0;
    }

    @Override
    public BigDecimal getTotalRevenue() {
        List<Booking> confirmedBookings = bookingRepository.findByStatus(BookingStatus.CONFIRMED);
        return confirmedBookings.stream()
                .map(Booking::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<BookingResponse> getBookingsByGuest(String guestName) {
        refreshHistory();
        return bookingHistory.stream()
                .filter(b -> b.getGuestName().equalsIgnoreCase(guestName))
                .collect(Collectors.toList());
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
