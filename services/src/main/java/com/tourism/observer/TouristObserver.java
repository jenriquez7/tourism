package com.tourism.observer;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.tourism.event.BookingStatusEvent;
import com.tourism.model.MessageType;
import com.tourism.model.Notification;
import com.tourism.service.NotificationService;
import com.tourism.util.MessageConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TouristObserver {

   private final NotificationService notificationService;

   @Async
   @EventListener
   public void notifyStatusChange(BookingStatusEvent event) {
      log.info(MessageConstants.TOURIST_NOTIFICATION, event.tourist().getFirstName(), event.bookingId());
      StringBuilder builder = new StringBuilder();
      switch (event.state()) {
         case CREATED:
            builder
                  .append(MessageConstants.CREATED_BOOKING_TOURIST)
                  .append(event.lodgingName())
                  .append(". ")
                  .append(MessageConstants.BOOKING_ID_IS)
                  .append(event.bookingId());
            break;
         case PENDING_PAYMENT:
            builder
                  .append(MessageConstants.PENDING_BOOKING_TOURIST)
                  .append(event.lodgingName())
                  .append(". ")
                  .append(MessageConstants.BOOKING_ID_IS)
                  .append(event.bookingId());
            break;
         case REJECTED:
            builder
                  .append(MessageConstants.YOUR_BOOKING_IN)
                  .append(event.lodgingName())
                  .append(" - ")
                  .append(event.bookingId())
                  .append(MessageConstants.REJECTED_BOOKING_TOURIST);
            break;
         case ACCEPTED:
            builder.append(MessageConstants.ACCEPTED_BOOKING_TOURIST).append(event.lodgingName()).append(" - ").append(event.bookingId());
            break;
         case EXPIRED:
            builder.append(MessageConstants.YOUR_BOOKING_IN).append(event.lodgingName()).append(" - ").append(MessageConstants.EXPIRED_BOOKING);
            break;
         case UNAVAILABLE:
            builder.append(MessageConstants.YOUR_BOOKING_IN).append(event.lodgingName()).append(" - ").append(MessageConstants.ERROR_ENOUGH_CAPACITY);
            break;
      }
      log.info(builder.toString());
      notificationService.createNotification(new Notification(event.tourist(), builder.toString(), MessageType.EMAIL));
   }

}
