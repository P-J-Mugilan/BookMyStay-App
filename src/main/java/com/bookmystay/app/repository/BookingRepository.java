package com.bookmystay.app.repository;

import com.bookmystay.app.entity.Booking;
import com.bookmystay.app.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByGuestNameIgnoreCase(String guestName);

}
