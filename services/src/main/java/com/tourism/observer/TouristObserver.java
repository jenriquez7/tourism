package com.tourism.observer;

import com.tourism.model.*;
import com.tourism.service.NotificationService;
import com.tourism.util.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TouristObserver implements BookingObserver {

    private final NotificationService notificationService;

    @Autowired
    public TouristObserver(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void notifyStatusChange(Booking booking, BookingState state) {
        Tourist tourist = booking.getTourist();
        StringBuilder builder = new StringBuilder();
        switch (state) {
            case CREATED:
                builder.append(MessageConstants.CREATED_BOOKING_TOURIST)
                        .append(booking.getLodging().getName()).append(". ")
                        .append(MessageConstants.BOOKING_ID_IS).append(booking.getId());
                break;
            case PENDING:
                builder.append(MessageConstants.PENDING_BOOKING_TOURIST)
                        .append(booking.getLodging().getName()).append(". ")
                        .append(MessageConstants.BOOKING_ID_IS).append(booking.getId());
                break;
            case REJECTED:
                builder.append(MessageConstants.YOUR_BOOKING_IN)
                        .append(booking.getLodging().getName()).append(" - ")
                        .append(booking.getId()).append(MessageConstants.REJECTED_BOOKING_TOURIST);
                break;
            case ACCEPTED:
                builder.append(booking.getId()).append(MessageConstants.ACCEPTED_BOOKING_TOURIST)
                        .append(booking.getLodging().getName()).append(" - ").append(booking.getId());
                break;
            case EXPIRED:
                builder.append(MessageConstants.YOUR_BOOKING_IN)
                        .append(booking.getLodging().getName()).append(" - ")
                        .append(MessageConstants.EXPIRED_BOOKING);
                break;

        }
        notificationService.createNotification(new Notification(tourist, builder.toString(), MessageType.EMAIL));
    }
}
