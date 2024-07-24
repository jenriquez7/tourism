package com.tourism.util.helpers;

import com.tourism.model.Lodging;

import java.time.LocalDate;
import java.util.List;

@FunctionalInterface
public interface PriceCalculator {
    double calculatePrice(Lodging lodging, List<LocalDate> dates, int adults, int children, int babies);
}
