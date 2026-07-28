package com.bookmystay.app.repository;

import com.bookmystay.app.entity.HotelService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HotelServiceRepository extends JpaRepository<HotelService, Long> {

    Optional<HotelService> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

}
