package com.tourism.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourism.model.Booking;
import com.tourism.model.BookingState;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

   List<Booking> findAllByOrderByCheckInAsc();

   List<Booking> findByCheckInLessThanAndStateIn(LocalDate checkInDate, List<BookingState> states);

   boolean existsByIdempotencyKey(String key);

}
