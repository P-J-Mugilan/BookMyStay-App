package com.bookmystay.app.repository;

import com.bookmystay.app.entity.Booking;
import com.bookmystay.app.entity.BookingAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingAddonRepository extends JpaRepository<BookingAddon, Long> {

    List<BookingAddon> findByBooking(Booking booking);

    List<BookingAddon> findByBookingId(Long bookingId);

}
