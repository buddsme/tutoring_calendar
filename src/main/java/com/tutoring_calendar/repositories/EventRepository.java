package com.tutoring_calendar.repositories;

import com.tutoring_calendar.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
