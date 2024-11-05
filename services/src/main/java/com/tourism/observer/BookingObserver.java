package com.tourism.observer;

import com.tourism.model.BookingState;
import com.tourism.model.LodgingOwner;
import com.tourism.model.Tourist;

import java.util.UUID;

public interface BookingObserver {
    void notifyStatusChange(String lodgingName, UUID bookingId, Tourist tourist, LodgingOwner owner, BookingState state);
}
