package com.tourism.jobs;

import com.tourism.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BookingJobs {

    private final BookingService bookingService;

    @Autowired
    public BookingJobs(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void expireBookings() {
        bookingService.updateToExpiredBookings();
    }
}
