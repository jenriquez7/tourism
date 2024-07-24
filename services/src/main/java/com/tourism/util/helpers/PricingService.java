package com.tourism.util.helpers;

import com.tourism.model.Lodging;
import com.tourism.model.TouristType;
import com.tourism.util.MessageConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PricingService {

    private final Map<TouristType, PriceCalculator> calculators;

    @Autowired
    public PricingService() {
        this.calculators = new EnumMap<>(TouristType.class);
        initializeCalculators();
    }

    public double calculateBookingPrice(TouristType type, Lodging lodging, List<LocalDate> dates, int adults, int children, int babies) {
        PriceCalculator calculator = calculators.get(type);
        if (calculator == null) {
            throw new IllegalArgumentException(MessageConstants.ERROR_PRICING_METHOD_NOT_FOUND + type);
        }
        return calculator.calculatePrice(lodging, dates, adults, children, babies);
    }

    private void initializeCalculators() {
        calculators.put(TouristType.STANDARD, (lodging, dates, adults, children, babies) ->
            dates.stream().mapToDouble(date -> {
                double nightPrice = lodging.getNightPrice();
                DayOfWeek dayOfWeek = date.getDayOfWeek();
                if (dayOfWeek == DayOfWeek.FRIDAY || dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                    nightPrice = lodging.getNightPrice() * 1.2;
                }
                return this.getNightPrice(nightPrice, adults, children, babies);
            }).sum()
        );

        calculators.put(TouristType.PREMIUM, (lodging, dates, adults, children, babies) ->
            dates.stream().mapToDouble(d -> this.getNightPrice(lodging.getNightPrice(), adults, children, babies)).sum()
        );
    }

    private double getNightPrice(double nightPrice, int adults, int children, int babies) {
        return (nightPrice * adults) + (nightPrice * children * 0.5) + (nightPrice * babies * 0.25);
    }
}
