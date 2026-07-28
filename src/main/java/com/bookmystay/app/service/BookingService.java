package com.bookmystay.app.service;

import com.bookmystay.app.dto.reponse.BookingResponse;
import com.bookmystay.app.dto.request.CreateBookingRequest;

import java.util.List;

public interface BookingService {

    BookingResponse submitBookingRequest(CreateBookingRequest request);

    List<BookingResponse> getBookingQueue();

    int getQueuePosition(Long bookingId);

}
