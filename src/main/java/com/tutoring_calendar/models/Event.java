package com.tutoring_calendar.models;

import com.tutoring_calendar.enums.EventStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "events")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "Client is mandatory")
    private Client client;

    @Column(name = "price")
    @NotNull(message = "Price is mandatory")
    private BigDecimal price;

    @Column(name = "date")
    @NotNull(message = "Date date is mandatory")
    private LocalDate date;

    @Column(name = "start_time")
    @NotNull(message = "Start time of event is mandatory")
    private LocalTime startTime;

    @Column(name = "finish_time")
    @NotNull(message = "Finish time of event is mandatory")
    private LocalTime finishTime;

    @Column(name = "repeatable")
    private boolean repeatable;

    @Column(name = "status")
    private EventStatus eventStatus;

    @Column(name = "original_id")
    private Long originalId;

    //Copy constructor
    public Event(Event otherEvent) {
        this.id = otherEvent.id;
        this.client = otherEvent.client;
        this.price = otherEvent.price;
        this.date = otherEvent.date;
        this.startTime = otherEvent.startTime;
        this.finishTime = otherEvent.finishTime;
        this.repeatable = otherEvent.repeatable;
        this.eventStatus = otherEvent.eventStatus;
    }

}
