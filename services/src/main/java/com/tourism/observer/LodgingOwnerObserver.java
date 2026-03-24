package com.tourism.observer;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.tourism.model.BookingState;
import com.tourism.model.LodgingOwner;
import com.tourism.model.MessageType;
import com.tourism.model.Notification;
import com.tourism.model.Tourist;
import com.tourism.service.NotificationService;
import com.tourism.util.MessageConstants;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LodgingOwnerObserver implements BookingObserver {

   private final NotificationService notificationService;

   @Override
   public void notifyStatusChange(String lodgingName, UUID bookingId, Tourist tourist, LodgingOwner owner, BookingState state) {
      StringBuilder builder = new StringBuilder();
      switch (state) {
         case CREATED:
            builder
                  .append(MessageConstants.CREATED_BOOKING_OWNER)
                  .append(lodgingName)
                  .append(". ")
                  .append(MessageConstants.BOOKING_ID_IS)
                  .append(bookingId);
            break;
         case PENDING:
            builder
                  .append(MessageConstants.PENDING_BOOKING_OWNER)
                  .append(lodgingName)
                  .append(". ")
                  .append(MessageConstants.BOOKING_ID_IS)
                  .append(bookingId);
            break;
         case REJECTED:
            builder.append(MessageConstants.REJECTED_BOOKING_OWNER).append(lodgingName).append(" - ").append(bookingId);
            break;
         case ACCEPTED:
            builder.append(MessageConstants.ACCEPTED_BOOKING_OWNER).append(lodgingName).append(" - ").append(bookingId);
            break;
         case EXPIRED:
            builder.append(MessageConstants.THE_BOOKING_IN).append(lodgingName).append(" - ").append(MessageConstants.EXPIRED_BOOKING);
            break;
      }
      notificationService.createNotification(new Notification(owner, builder.toString(), MessageType.EMAIL));
   }

}
