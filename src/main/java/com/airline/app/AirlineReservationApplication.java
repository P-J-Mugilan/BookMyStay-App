package com.airline.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AirlineReservationApplication {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("Welcome to Airline Ticket Reservation System!");
        System.out.println("=================================================");
        SpringApplication.run(AirlineReservationApplication.class, args);
    }

}
