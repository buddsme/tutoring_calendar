package com.tutoring_calendar.dto;

import com.tutoring_calendar.enums.EventStatus;
import com.tutoring_calendar.models.Client;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EventUpdateDTO {
    private Long id;
    private Client client;
    private BigDecimal price;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime finishTime;
    private boolean repeatable;
    private EventStatus eventStatus;
    private Long originalId;
}
