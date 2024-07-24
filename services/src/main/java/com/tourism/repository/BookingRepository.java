package com.tourism.repository;

import com.tourism.model.Booking;
import com.tourism.model.BookingState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findAllByOrderByCheckInAsc();
    List<Booking> findByCheckInLessThanAndStateIn(LocalDate checkInDate, List<BookingState> states);
}
