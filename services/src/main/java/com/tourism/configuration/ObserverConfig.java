package com.tourism.configuration;

import com.tourism.observer.LodgingOwnerObserver;
import com.tourism.observer.TouristObserver;
import com.tourism.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObserverConfig {

    @Autowired
    public void configureObservers(BookingService bookingService,
                                   LodgingOwnerObserver lodgingOwnerObserver,
                                   TouristObserver touristObserver) {
        bookingService.addObserver(lodgingOwnerObserver);
        bookingService.addObserver(touristObserver);
    }
}
