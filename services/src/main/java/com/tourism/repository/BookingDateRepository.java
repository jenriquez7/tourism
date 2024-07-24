package com.tourism.repository;


import com.tourism.model.Booking;
import com.tourism.model.BookingDate;
import com.tourism.model.BookingState;
import com.tourism.model.Lodging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookingDateRepository extends JpaRepository<BookingDate, UUID> {

    @Query("SELECT bd FROM BookingDate bd JOIN bd.booking b WHERE b.lodging = :lodging AND b.state = :state AND :date BETWEEN b.checkIn AND b.checkOut ORDER BY b.checkIn ASC")
    List<BookingDate> findBookingDatesByLodgingAndStateAndDateBetweenCheckInAndCheckOutOrderByCheckInAsc(@Param("lodging") Lodging lodging, @Param("date") LocalDate date, @Param("state") BookingState state);

    @Query("SELECT bd FROM BookingDate bd JOIN bd.booking b WHERE b.lodging = :lodging AND b.state = :state")
    LocalDate findLastBookingDateByLodgingAndState(@Param("lodging") Lodging lodging, @Param("state") BookingState state);

    @Modifying
    @Query("DELETE FROM BookingDate bd WHERE bd.booking = :booking")
    void deleteByBooking(@Param("booking") Booking booking);

}
