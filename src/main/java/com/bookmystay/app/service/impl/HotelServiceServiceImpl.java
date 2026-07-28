package com.bookmystay.app.service.impl;

import com.bookmystay.app.dto.reponse.BookingResponse;
import com.bookmystay.app.dto.request.AddServiceRequest;
import com.bookmystay.app.entity.Booking;
import com.bookmystay.app.entity.BookingAddon;
import com.bookmystay.app.entity.HotelService;
import com.bookmystay.app.exception.ResourceNotFoundException;
import com.bookmystay.app.repository.BookingAddonRepository;
import com.bookmystay.app.repository.BookingRepository;
import com.bookmystay.app.repository.HotelServiceRepository;
import com.bookmystay.app.service.HotelServiceService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelServiceServiceImpl implements HotelServiceService {

    private final HotelServiceRepository hotelServiceRepository;
    private final BookingAddonRepository bookingAddonRepository;
    private final BookingRepository bookingRepository;

    // In-memory one-to-many map: Booking ID -> List of Services as per UC5 requirement
    private final ConcurrentHashMap<Long, List<HotelService>> reservationServices = new ConcurrentHashMap<>();

    @PostConstruct
    @Transactional
    public void initializeServices() {
        // Seed default services if empty
        if (hotelServiceRepository.count() == 0) {
            hotelServiceRepository.save(new HotelService(null, "Breakfast", BigDecimal.valueOf(15.00)));
            hotelServiceRepository.save(new HotelService(null, "Spa", BigDecimal.valueOf(50.00)));
            hotelServiceRepository.save(new HotelService(null, "Airport Pickup", BigDecimal.valueOf(30.00)));
        }

        // Load existing mapping into in-memory structure
        List<BookingAddon> addons = bookingAddonRepository.findAll();
        reservationServices.clear();
        for (BookingAddon addon : addons) {
            reservationServices.computeIfAbsent(addon.getBooking().getId(), k -> new ArrayList<>())
                    .add(addon.getHotelService());
        }
    }

    @Override
    @Transactional
    public BookingResponse addServiceToBooking(Long bookingId, AddServiceRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        HotelService service = hotelServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Requested service does not exist."));

        BookingAddon addon = BookingAddon.builder()
                .booking(booking)
                .hotelService(service)
                .priceCharged(service.getPrice())
                .build();
        bookingAddonRepository.save(addon);

        // Update booking's total cost
        booking.setTotalCost(booking.getTotalCost().add(service.getPrice()));
        Booking savedBooking = bookingRepository.save(booking);

        // Update in-memory map
        reservationServices.computeIfAbsent(bookingId, k -> new ArrayList<>()).add(service);

        return mapToResponse(savedBooking);
    }

    @Override
    public List<HotelService> getAllServices() {
        return hotelServiceRepository.findAll();
    }

    @Override
    @Transactional
    public HotelService createService(HotelService service) {
        return hotelServiceRepository.save(service);
    }

    // Helper method to retrieve cached services list
    public List<HotelService> getCachedServices(Long bookingId) {
        return reservationServices.getOrDefault(bookingId, new ArrayList<>());
    }

    private BookingResponse mapToResponse(Booking booking) {
        List<String> serviceNames = reservationServices.getOrDefault(booking.getId(), new ArrayList<>()).stream()
                .map(HotelService::getName)
                .collect(Collectors.toList());

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
