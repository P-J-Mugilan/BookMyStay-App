package com.bookmystay.app.constants;

public final class ValidationConstants {

    private ValidationConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final int MIN_ROOM_COUNT = 0;

    public static final int MAX_ROOM_NAME_LENGTH = 50;

    public static final int MIN_GUESTS = 1;

    public static final int MAX_GUESTS_PER_ROOM = 4;

    public static final double MIN_ROOM_PRICE = 1.0;

}