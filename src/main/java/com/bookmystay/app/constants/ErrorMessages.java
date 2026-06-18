package com.bookmystay.app.constants;

public final class ErrorMessages {

    private ErrorMessages() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String ROOM_NOT_FOUND =
            "Requested room type does not exist.";

    public static final String BOOKING_NOT_FOUND =
            "Booking not found.";

    public static final String SERVICE_NOT_FOUND =
            "Requested service does not exist.";

    public static final String EMPTY_BOOKING_QUEUE =
            "No bookings currently waiting in the pipeline.";

    public static final String INSUFFICIENT_INVENTORY =
            "No rooms available for the selected room type.";

    public static final String INVALID_ROOM_COUNT =
            "Room count cannot be negative.";

    public static final String INVALID_PRICE =
            "Price must be greater than zero.";

    public static final String INVALID_CHECK_IN_DATE =
            "Check-in date cannot be in the past.";

    public static final String INVALID_CHECK_OUT_DATE =
            "Check-out date must be after check-in date.";

    public static final String INVALID_NUMBER_OF_GUESTS =
            "Invalid number of guests.";

    public static final String DUPLICATE_ROOM_TYPE =
            "Room type already exists.";

}