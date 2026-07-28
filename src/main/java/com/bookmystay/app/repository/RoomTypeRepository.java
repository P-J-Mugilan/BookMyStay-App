package com.bookmystay.app.repository;

import com.bookmystay.app.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    Optional<RoomType> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

}
