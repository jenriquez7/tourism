package com.tourism.test.util;

import com.tourism.util.validations.DateValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class DateValidationTests {

    @InjectMocks
    private DateValidation dateValidation;

    private LocalDate checkIn;
    private LocalDate checkOut;

    @BeforeEach
    void setUp() {
        checkIn = LocalDate.now();
        checkOut = LocalDate.now().plusDays(4);
    }

    @Test
    @DisplayName("Dates Between Dates - Same Dates")
    void testDatesBetweenDatesSameDates() {
        assertThrows(IllegalArgumentException.class, () -> dateValidation.datesBetweenDates(checkIn, checkIn));
    }

    @Test
    @DisplayName("Dates Between Dates - Two Days Difference")
    void testDatesBetweenDatesTwoDaysDifference() {
        List<LocalDate> result = dateValidation.datesBetweenDates(checkIn, checkOut);

        assertEquals(4, result.size());
        assertEquals(checkIn, result.get(0));
        assertEquals(checkIn.plusDays(1), result.get(1));
        assertEquals(checkOut.minusDays(1), result.getLast());
    }

    @Test
    @DisplayName("Dates Between Dates - Long Period")
    void testDatesBetweenDatesLongPeriod() {
        checkOut = checkIn.plusDays(365);
        List<LocalDate> result = dateValidation.datesBetweenDates(checkIn, checkOut);

        assertEquals(365, result.size());
        assertEquals(checkIn, result.getFirst());
        assertEquals(checkOut.minusDays(1), result.getLast());
    }

    @Test
    @DisplayName("Dates Between Dates - Invalid Dates")
    void testDatesBetweenDatesInvalidDates() {
        checkOut = checkIn.minusDays(2);
        assertThrows(IllegalArgumentException.class, () -> dateValidation.datesBetweenDates(checkIn, checkOut));
    }

    @Test
    @DisplayName("Check In Before Today")
    void testCheckInBeforeToday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        assertTrue(dateValidation.checkInBeforeToday(yesterday));
        assertFalse(dateValidation.checkInBeforeToday(tomorrow));
    }

    @Test
    @DisplayName("Check Out Before Check In")
    void testCheckOutBeforeCheckIn() {
        LocalDate earlier = LocalDate.of(2024, 7, 11);
        LocalDate later = LocalDate.of(2024, 7, 13);

        assertTrue(dateValidation.checkOutBeforeCheckIn(later, earlier));
        assertTrue(dateValidation.checkOutBeforeCheckIn(earlier, earlier));
        assertFalse(dateValidation.checkOutBeforeCheckIn(earlier, later));
    }
}
