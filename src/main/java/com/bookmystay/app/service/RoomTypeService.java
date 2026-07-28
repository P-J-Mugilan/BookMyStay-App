package com.bookmystay.app.service;

import com.bookmystay.app.dto.reponse.RoomTypeResponse;
import com.bookmystay.app.dto.request.RoomTypeRequest;

import java.util.List;

public interface RoomTypeService {

    RoomTypeResponse createRoomType(RoomTypeRequest request);

    List<RoomTypeResponse> getAllRoomTypes();

    RoomTypeResponse getRoomTypeById(Long id);

    RoomTypeResponse updateRoomType(Long id, RoomTypeRequest request);

    void deleteRoomType(Long id);

}
