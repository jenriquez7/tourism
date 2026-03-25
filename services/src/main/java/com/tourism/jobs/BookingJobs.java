package com.tourism.jobs;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tourism.service.BookingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingJobs {

   private final BookingService bookingService;

   @Scheduled(cron = "0 0 0 * * ?", zone = "America/Montevideo")
   @SchedulerLock(name = "expireBookingsLock", lockAtMostFor = "10m", lockAtLeastFor = "1m")
   public void expireBookings() {
      bookingService.updateToExpiredBookings();
   }

}
