package com.tourism.util.validations;

import com.tourism.util.MessageConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class DateValidation {

    private DateValidation(){}

    public List<LocalDate> datesBetweenDates(LocalDate checkIn, LocalDate checkOut) {
        if (!this.checkInBeforeToday(checkIn) && !this.checkOutBeforeCheckIn(checkIn, checkOut)) {
            return Stream.iterate(checkIn, date -> date.plusDays(1))
                    .limit(ChronoUnit.DAYS.between(checkIn, checkOut)).toList();
        } else {
            throw new IllegalArgumentException(MessageConstants.ERROR_BOOKING_DATES);
        }
    }

    public boolean checkInBeforeToday(LocalDate checkIn) {
        return checkIn.isBefore(LocalDate.now());
    }

    public boolean checkOutBeforeCheckIn(LocalDate checkIn, LocalDate checkOut) {
        return checkIn.isAfter(checkOut) || checkIn.equals(checkOut);
    }
}
