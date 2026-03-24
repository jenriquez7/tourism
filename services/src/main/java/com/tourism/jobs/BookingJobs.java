package com.tourism.jobs;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tourism.service.BookingService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookingJobs {

   private final BookingService bookingService;

   @Scheduled(cron = "0 0 0 * * ?")
   public void expireBookings() {
      bookingService.updateToExpiredBookings();
   }

}
