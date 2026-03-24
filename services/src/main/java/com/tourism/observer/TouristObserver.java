package com.tourism.observer;

import com.tourism.model.*;
import com.tourism.service.NotificationService;
import com.tourism.util.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TouristObserver implements BookingObserver {

    private final NotificationService notificationService;

    @Autowired
    public TouristObserver(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void notifyStatusChange(String lodgingName, UUID bookingId, Tourist tourist, LodgingOwner owner, BookingState state) {
        StringBuilder builder = new StringBuilder();
        switch (state) {
            case CREATED:
                builder.append(MessageConstants.CREATED_BOOKING_TOURIST)
                        .append(lodgingName).append(". ")
                        .append(MessageConstants.BOOKING_ID_IS).append(bookingId);
                break;
            case PENDING:
                builder.append(MessageConstants.PENDING_BOOKING_TOURIST)
                        .append(lodgingName).append(". ")
                        .append(MessageConstants.BOOKING_ID_IS).append(bookingId);
                break;
            case REJECTED:
                builder.append(MessageConstants.YOUR_BOOKING_IN)
                        .append(lodgingName).append(" - ")
                        .append(bookingId).append(MessageConstants.REJECTED_BOOKING_TOURIST);
                break;
            case ACCEPTED:
                builder.append(MessageConstants.ACCEPTED_BOOKING_TOURIST)
                        .append(lodgingName).append(" - ").append(bookingId);
                break;
            case EXPIRED:
                builder.append(MessageConstants.YOUR_BOOKING_IN)
                        .append(lodgingName).append(" - ")
                        .append(MessageConstants.EXPIRED_BOOKING);
                break;
            case UNAVAILABLE:
                builder.append(MessageConstants.YOUR_BOOKING_IN)
                        .append(lodgingName).append(" - ")
                        .append(MessageConstants.ERROR_ENOUGH_CAPACITY);
                break;
        }
        notificationService.createNotification(new Notification(tourist, builder.toString(), MessageType.EMAIL));
    }
}
