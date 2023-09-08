package com.tutoring_calendar.repositories;

import com.tutoring_calendar.models.Client;
import com.tutoring_calendar.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.date BETWEEN :startDate AND :endDate")
    List<Event> findAllByDateRange(@Param("startDate")LocalDate startDate, @Param("endDate")LocalDate endDate);

    @Query("SELECT e FROM Event e WHERE e.client=:client")
    List<Event> findAllByClient(@Param("client")Client client);
}
