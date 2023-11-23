package com.tutoring_calendar.utility;

import com.tutoring_calendar.enums.ClientStatus;
import com.tutoring_calendar.enums.EventStatus;
import com.tutoring_calendar.models.Client;
import com.tutoring_calendar.models.Event;
import com.tutoring_calendar.repositories.ClientRepository;
import com.tutoring_calendar.repositories.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
public class TestDataInitializer {

    @Bean
    public CommandLineRunner initializeTestData(ClientRepository clientRepository, EventRepository eventRepository) {
        return args -> {
            Client client1 = clientRepository.save(new Client(1L, "Artem Denysiuk", BigDecimal.ZERO, ClientStatus.ACTIVE));
            Client client2 = clientRepository.save(new Client(2L, "Max Stryzheus", BigDecimal.ZERO, ClientStatus.ARCHIVED));
            Client client3 = clientRepository.save(new Client(3L, "Vlad Pozniak", BigDecimal.ZERO, ClientStatus.ACTIVE));
            Client client4 = clientRepository.save(new Client(4L, "Bogdan Hodunok", BigDecimal.ZERO, ClientStatus.ARCHIVED));

            eventRepository.save(new Event(1L, client1, BigDecimal.valueOf(200), LocalDate.of(2023,10,1), LocalTime.of(13,0), LocalTime.of(14, 0), true, EventStatus.CREATED, 1L));
            eventRepository.save(new Event(2L, client2, BigDecimal.valueOf(200), LocalDate.of(2023,10,7), LocalTime.of(13,0), LocalTime.of(14, 0), false, EventStatus.CREATED, 2L));
            eventRepository.save(new Event(3L, client3, BigDecimal.valueOf(200), LocalDate.of(2023,10,15), LocalTime.of(13,0), LocalTime.of(14, 0), true, EventStatus.CREATED, 3L));
            eventRepository.save(new Event(4L, client4, BigDecimal.valueOf(200), LocalDate.of(2023,10,20), LocalTime.of(13,0), LocalTime.of(14, 0), true, EventStatus.CREATED, 4L));
            eventRepository.save(new Event(5L, client1, BigDecimal.valueOf(200), LocalDate.of(2023,10,26), LocalTime.of(13,0), LocalTime.of(14, 0), false, EventStatus.CREATED, 5L));
            eventRepository.save(new Event(6L, client2, BigDecimal.valueOf(200), LocalDate.of(2023,10,21), LocalTime.of(13,0), LocalTime.of(14, 0), false, EventStatus.CREATED, 2L));
            // Add more clients as needed
        };
    }
}
