package com.tutoring_calendar.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
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
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @Column(name = "price")
    private Integer price;
    @Column(name = "date")
    private LocalDate date;
    @Column(name = "start_time")
    private LocalTime startTime;
    @Column(name = "finish_time")
    private LocalTime finishTime;
    @Column(name = "repeatable")
    private boolean repeatable;
}
