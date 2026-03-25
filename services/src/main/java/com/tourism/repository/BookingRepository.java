package com.tourism.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tourism.model.Booking;
import com.tourism.model.BookingState;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

   List<Booking> findAllByOrderByCheckInAsc();

   boolean existsByIdempotencyKey(String key);

   @Modifying
   @Query("UPDATE Booking b SET b.state = 'EXPIRED' " + "WHERE b.checkIn < :date " + "AND b.state IN :states " + "AND b.state != 'EXPIRED'")
   int expireBookingsAutomatic(@Param("date") LocalDate date, @Param("states") List<BookingState> states);

}
