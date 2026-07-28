package com.bookmystay.app.service;

import com.bookmystay.app.dto.reponse.BookingResponse;
import com.bookmystay.app.dto.request.AddServiceRequest;
import com.bookmystay.app.entity.HotelService;

import java.util.List;

public interface HotelServiceService {

    BookingResponse addServiceToBooking(Long bookingId, AddServiceRequest request);

    List<HotelService> getAllServices();

    HotelService createService(HotelService service);

}
