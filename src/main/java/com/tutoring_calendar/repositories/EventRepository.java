package com.tutoring_calendar.repositories;

import com.tutoring_calendar.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {


    @Query("SELECT e FROM Event e WHERE e.date BETWEEN :startOfTheWeek AND :endOfTheWeek")
    List<Event> findAllByWeekRange(@Param("startOfTheWeek")LocalDate startOfTheWeek, @Param("endOfTheWeek")LocalDate endOfTheWeek);
}
