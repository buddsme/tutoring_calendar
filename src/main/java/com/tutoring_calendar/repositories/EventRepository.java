package com.tutoring_calendar.repositories;

import com.tutoring_calendar.models.Client;
import com.tutoring_calendar.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.date BETWEEN :firstDayOfWeek AND :lastDayOfWeek")
    List<Event> findAllByDateRange(@Param("firstDayOfWeek") LocalDate firstDay, @Param("lastDayOfWeek") LocalDate lastDay);

    @Query("SELECT e FROM Event e WHERE e.client=:client")
    List<Event> findAllByClient(@Param("client") Client client);

    @Query("SELECT e FROM Event e WHERE e.id = e.originalId")
    List<Event> findAllOriginalEvents();

    @Query("SELECT e FROM Event e WHERE e.originalId = :id AND e.id != :id AND e.date BETWEEN :firstDayOfWeek AND :lastDayOfWeek")
    Optional<Event> findRecurringEventOfSelectedWeek(@Param("id") Long id, @Param("firstDayOfWeek") LocalDate firstDay, @Param("lastDayOfWeek") LocalDate lastDay);
}
