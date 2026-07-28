package com.bookmystay.app.repository;

import com.bookmystay.app.entity.Room;
import com.bookmystay.app.entity.RoomType;
import com.bookmystay.app.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByRoomTypeAndStatus(RoomType roomType, RoomStatus status);

    boolean existsByRoomNumber(String roomNumber);

}
