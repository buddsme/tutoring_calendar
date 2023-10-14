package com.tutoring_calendar.utility;

import com.tutoring_calendar.enums.ClientStatus;
import com.tutoring_calendar.models.Client;
import com.tutoring_calendar.repositories.ClientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.math.BigDecimal;

@Configuration
public class TestDataInitializer {

    @Bean
    public CommandLineRunner initializeTestData(ClientRepository clientRepository) {
        return args -> {
            clientRepository.save(new Client(1L, "Artem Denysiuk", BigDecimal.ZERO, ClientStatus.ACTIVE));
            clientRepository.save(new Client(2L, "Max Stryzheus", BigDecimal.ZERO, ClientStatus.ARCHIVED));
            clientRepository.save(new Client(3L, "Vlad Pozniak", BigDecimal.ZERO, ClientStatus.ACTIVE));
            clientRepository.save(new Client(4L, "Bogdan Hodunok", BigDecimal.ZERO, ClientStatus.ARCHIVED));
            // Add more clients as needed
        };
    }
}
