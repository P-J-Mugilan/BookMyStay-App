package com.bookmystay.app.service;

import com.bookmystay.app.dto.reponse.BookingResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ReportService {

    List<BookingResponse> getBookingHistory();

    double getOccupancyRate();

    BigDecimal getTotalRevenue();

    List<BookingResponse> getBookingsByGuest(String guestName);

}
