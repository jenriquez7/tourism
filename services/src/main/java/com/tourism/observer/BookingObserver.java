package com.tourism.observer;

import com.tourism.model.Booking;
import com.tourism.model.BookingState;

public interface BookingObserver {
    void notifyStatusChange(Booking booking, BookingState state);
}
