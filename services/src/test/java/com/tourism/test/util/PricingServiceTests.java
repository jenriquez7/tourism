package com.tourism.test.util;

import com.tourism.model.*;
import com.tourism.util.helpers.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class PricingServiceTests {

    private Lodging lodging;

    @InjectMocks
    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        lodging = new Lodging("Hotel Test", "Un hotel de pruebas", "Calle falsa 123",
                "+59899123456", 5, 25.0, 4, new TouristicPlace(), new LodgingOwner(),
                true);
        lodging.setId(UUID.randomUUID());
        lodging.setNightPrice(100.0);
    }


    @Test
    @DisplayName("Calculate Price Standard - Weekdays Only")
    void testCalculateBookingPriceStandardWeekdaysOnly() {
        lodging.setNightPrice(100.0);

        List<LocalDate> dates = Arrays.asList(
                LocalDate.of(2024, 7, 1), // Monday
                LocalDate.of(2024, 7, 2), // Tuesday
                LocalDate.of(2024, 7, 3)  // Wednesday
        );

        double price = pricingService.calculateBookingPrice(TouristType.STANDARD, lodging, dates, 2, 1, 1);

        // Expected price: (100 * 2 + 100 * 0.5 + 100 * 0.25) * 3 = 825.0
        assertEquals(825.0, price, 0.01);
    }

    @Test
    @DisplayName("Calculate Price Standard - Weekend Included")
    void testCalculateBookingStandardPriceWeekendIncluded() {
        lodging.setNightPrice(100.0);

        List<LocalDate> dates = Arrays.asList(
                LocalDate.of(2024, 7, 5), // Friday
                LocalDate.of(2024, 7, 6), // Saturday
                LocalDate.of(2024, 7, 7)  // Sunday
        );

        double price = pricingService.calculateBookingPrice(TouristType.STANDARD, lodging, dates, 2, 1, 1);

        // Expected price: (120 * 2 + 120 * 0.5 + 120 * 0.25) * 3 = 990.0
        assertEquals(990.0, price, 0.01);
    }

    @Test
    @DisplayName("Calculate Price Standard - Mixed Days")
    void testCalculateBookingStandardPriceMixedDays() {
        lodging.setNightPrice(100.0);

        List<LocalDate> dates = Arrays.asList(
                LocalDate.of(2024, 7, 4), // Thursday
                LocalDate.of(2024, 7, 5), // Friday
                LocalDate.of(2024, 7, 6)  // Saturday
        );

        double price = pricingService.calculateBookingPrice(TouristType.STANDARD, lodging, dates, 2, 1, 1);

        // Expected price: (100 * 2 + 100 * 0.5 + 100 * 0.25) + (120 * 2 + 120 * 0.5 + 120 * 0.25) * 2 = 935.0
        assertEquals(935.0, price, 0.01);
    }

    @ParameterizedTest
    @MethodSource("providePremiumTestCases")
    @DisplayName("Calculate Price Premium - Various Scenarios")
    void testCalculateBookingPricePremium(String scenario, List<LocalDate> dates, double expectedPrice) {
        double price = pricingService.calculateBookingPrice(TouristType.PREMIUM, lodging, dates, 2, 1, 1);
        assertEquals(expectedPrice, price, 0.01, "Failed for scenario: " + scenario);
    }

    private static Stream<Arguments> providePremiumTestCases() {
        return Stream.of(
                Arguments.of("Weekdays Only",
                        Arrays.asList(
                                LocalDate.of(2024, 7, 1), // Monday
                                LocalDate.of(2024, 7, 2), // Tuesday
                                LocalDate.of(2024, 7, 3)  // Wednesday
                        ),
                        825.0
                ),
                Arguments.of("Weekend Included",
                        Arrays.asList(
                                LocalDate.of(2024, 7, 5), // Friday
                                LocalDate.of(2024, 7, 6), // Saturday
                                LocalDate.of(2024, 7, 7)  // Sunday
                        ),
                        825.0
                ),
                Arguments.of("Mixed Days",
                        Arrays.asList(
                                LocalDate.of(2024, 7, 4), // Thursday
                                LocalDate.of(2024, 7, 5), // Friday
                                LocalDate.of(2024, 7, 6)  // Saturday
                        ),
                        825.0
                )
        );
    }
}
