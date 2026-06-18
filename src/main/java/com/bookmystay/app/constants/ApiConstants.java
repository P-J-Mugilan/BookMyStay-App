package com.bookmystay.app.constants;

public final class ApiConstants {

    private ApiConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String API_BASE_URL = "/api/v1";

    public static final String ROOMS = API_BASE_URL + "/rooms";
    public static final String BOOKINGS = API_BASE_URL + "/bookings";
    public static final String SERVICES = API_BASE_URL + "/services";
    public static final String REPORTS = API_BASE_URL + "/reports";

}