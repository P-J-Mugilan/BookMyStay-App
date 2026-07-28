package com.bookmystay.app.service.impl;

import com.bookmystay.app.dto.reponse.RoomTypeResponse;
import com.bookmystay.app.dto.request.RoomTypeRequest;
import com.bookmystay.app.entity.RoomType;
import com.bookmystay.app.exception.DuplicateResourceException;
import com.bookmystay.app.exception.ResourceNotFoundException;
import com.bookmystay.app.repository.RoomTypeRepository;
import com.bookmystay.app.service.RoomTypeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    // In-memory data structures for fast O(1) lookups as per UC requirements
    private final ConcurrentHashMap<String, Integer> roomInventory = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Double> roomPrices = new ConcurrentHashMap<>();

    @PostConstruct
    @Transactional
    public void initializeDefaultInventory() {
        // Pre-populate DB if empty
        if (roomTypeRepository.count() == 0) {
            saveDefaultRoomType("Single", 10, BigDecimal.valueOf(100.00), "A cozy room for one person.");
            saveDefaultRoomType("Double", 5, BigDecimal.valueOf(200.00), "A spacious room for two people.");
            saveDefaultRoomType("Suite", 2, BigDecimal.valueOf(500.00), "A luxurious suite with extra amenities.");
        }

        // Load all room types from DB into in-memory maps
        syncInMemoryCache();
    }

    private void saveDefaultRoomType(String name, Integer availableRooms, BigDecimal pricePerNight, String description) {
        RoomType roomType = RoomType.builder()
                .name(name)
                .availableRooms(availableRooms)
                .pricePerNight(pricePerNight)
                .description(description)
                .build();
        roomTypeRepository.save(roomType);
    }

    private void syncInMemoryCache() {
        List<RoomType> allRoomTypes = roomTypeRepository.findAll();
        roomInventory.clear();
        roomPrices.clear();
        for (RoomType rt : allRoomTypes) {
            String key = rt.getName().toLowerCase();
            roomInventory.put(key, rt.getAvailableRooms());
            roomPrices.put(key, rt.getPricePerNight().doubleValue());
        }
    }

    @Override
    @Transactional
    public RoomTypeResponse createRoomType(RoomTypeRequest request) {
        if (roomTypeRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Room type '" + request.getName() + "' already exists");
        }

        RoomType roomType = RoomType.builder()
                .name(request.getName())
                .availableRooms(request.getAvailableRooms())
                .pricePerNight(request.getPricePerNight())
                .description(request.getDescription())
                .build();

        RoomType saved = roomTypeRepository.save(roomType);

        // Update in-memory caches
        String key = saved.getName().toLowerCase();
        roomInventory.put(key, saved.getAvailableRooms());
        roomPrices.put(key, saved.getPricePerNight().doubleValue());

        return mapToResponse(saved);
    }

    @Override
    public List<RoomTypeResponse> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomTypeResponse> getAvailableRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .filter(rt -> getCachedInventory(rt.getName()) > 0)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoomTypeResponse getRoomTypeById(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found with id: " + id));
        return mapToResponse(roomType);
    }

    @Override
    @Transactional
    public RoomTypeResponse updateRoomType(Long id, RoomTypeRequest request) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found with id: " + id));

        // If name changes, check for uniqueness
        if (!roomType.getName().equalsIgnoreCase(request.getName()) &&
                roomTypeRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Room type '" + request.getName() + "' already exists");
        }

        // Remove old cache entry if name changed
        if (!roomType.getName().equalsIgnoreCase(request.getName())) {
            roomInventory.remove(roomType.getName().toLowerCase());
            roomPrices.remove(roomType.getName().toLowerCase());
        }

        roomType.setName(request.getName());
        roomType.setAvailableRooms(request.getAvailableRooms());
        roomType.setPricePerNight(request.getPricePerNight());
        roomType.setDescription(request.getDescription());

        RoomType updated = roomTypeRepository.save(roomType);

        // Update cache
        String key = updated.getName().toLowerCase();
        roomInventory.put(key, updated.getAvailableRooms());
        roomPrices.put(key, updated.getPricePerNight().doubleValue());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRoomType(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found with id: " + id));

        roomTypeRepository.delete(roomType);

        // Remove from cache
        String key = roomType.getName().toLowerCase();
        roomInventory.remove(key);
        roomPrices.remove(key);
    }

    // Cache access helper methods for future UCs
    public Integer getCachedInventory(String name) {
        return roomInventory.getOrDefault(name.toLowerCase(), 0);
    }

    public Double getCachedPrice(String name) {
        return roomPrices.get(name.toLowerCase());
    }

    public void updateCachedInventory(String name, Integer count) {
        roomInventory.put(name.toLowerCase(), count);
    }

    private RoomTypeResponse mapToResponse(RoomType roomType) {
        return RoomTypeResponse.builder()
                .id(roomType.getId())
                .name(roomType.getName())
                .availableRooms(roomType.getAvailableRooms())
                .pricePerNight(roomType.getPricePerNight())
                .description(roomType.getDescription())
                .build();
    }
}
