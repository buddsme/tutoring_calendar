package com.tutoring_calendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TutoringCalendarApplication {

    public static void main(String[] args) {
        SpringApplication.run(TutoringCalendarApplication.class, args);
    }
}
