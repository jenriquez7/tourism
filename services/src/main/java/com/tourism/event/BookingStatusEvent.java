package com.tourism.event;

import java.util.UUID;

import com.tourism.model.BookingState;
import com.tourism.model.LodgingOwner;
import com.tourism.model.Tourist;

public record BookingStatusEvent(String lodgingName, UUID bookingId, Tourist tourist, LodgingOwner owner, BookingState state) {

}
