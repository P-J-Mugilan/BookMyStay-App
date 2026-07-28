package com.bookmystay.app.service;

import com.bookmystay.app.dto.reponse.RoomTypeResponse;
import com.bookmystay.app.dto.request.RoomTypeRequest;
import com.bookmystay.app.exception.DuplicateResourceException;
import com.bookmystay.app.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RoomTypeServiceTests {

    @Autowired
    private RoomTypeService roomTypeService;

    @Test
    void testDefaultRoomTypesInitialized() {
        List<RoomTypeResponse> roomTypes = roomTypeService.getAllRoomTypes();
        assertNotNull(roomTypes);
        assertTrue(roomTypes.size() >= 3);

        boolean hasSingle = roomTypes.stream().anyMatch(rt -> rt.getName().equalsIgnoreCase("Single"));
        boolean hasDouble = roomTypes.stream().anyMatch(rt -> rt.getName().equalsIgnoreCase("Double"));
        boolean hasSuite = roomTypes.stream().anyMatch(rt -> rt.getName().equalsIgnoreCase("Suite"));

        assertTrue(hasSingle);
        assertTrue(hasDouble);
        assertTrue(hasSuite);
    }

    @Test
    void testCreateRoomTypeSuccess() {
        RoomTypeRequest request = new RoomTypeRequest("Deluxe", 8, BigDecimal.valueOf(350.00), "Deluxe Suite");
        RoomTypeResponse response = roomTypeService.createRoomType(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Deluxe", response.getName());
        assertEquals(8, response.getAvailableRooms());
        assertEquals(0, BigDecimal.valueOf(350.00).compareTo(response.getPricePerNight()));
    }

    @Test
    void testCreateDuplicateRoomTypeThrowsException() {
        RoomTypeRequest request = new RoomTypeRequest("Single", 12, BigDecimal.valueOf(110.00), "Another Single");
        assertThrows(DuplicateResourceException.class, () -> roomTypeService.createRoomType(request));
    }

    @Test
    void testGetRoomTypeByIdSuccess() {
        List<RoomTypeResponse> roomTypes = roomTypeService.getAllRoomTypes();
        Long firstId = roomTypes.get(0).getId();

        RoomTypeResponse response = roomTypeService.getRoomTypeById(firstId);
        assertNotNull(response);
        assertEquals(firstId, response.getId());
    }

    @Test
    void testGetRoomTypeByIdNotFoundThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> roomTypeService.getRoomTypeById(999L));
    }

    @Test
    void testUpdateRoomTypeSuccess() {
        List<RoomTypeResponse> roomTypes = roomTypeService.getAllRoomTypes();
        RoomTypeResponse target = roomTypes.stream()
                .filter(rt -> rt.getName().equalsIgnoreCase("Single"))
                .findFirst()
                .orElseThrow();

        RoomTypeRequest updateRequest = new RoomTypeRequest("Single Updated", 15, BigDecimal.valueOf(125.00), "Updated Single description");
        RoomTypeResponse updated = roomTypeService.updateRoomType(target.getId(), updateRequest);

        assertNotNull(updated);
        assertEquals("Single Updated", updated.getName());
        assertEquals(15, updated.getAvailableRooms());
        assertEquals(0, BigDecimal.valueOf(125.00).compareTo(updated.getPricePerNight()));
    }

    @Test
    void testDeleteRoomTypeSuccess() {
        RoomTypeRequest request = new RoomTypeRequest("TempRoom", 1, BigDecimal.valueOf(50.00), "Temp Room");
        RoomTypeResponse created = roomTypeService.createRoomType(request);

        assertNotNull(roomTypeService.getRoomTypeById(created.getId()));

        roomTypeService.deleteRoomType(created.getId());
        assertThrows(ResourceNotFoundException.class, () -> roomTypeService.getRoomTypeById(created.getId()));
    }
}
