package com.tutoring_calendar.controllers;

import com.tutoring_calendar.dto.EventResponse;
import com.tutoring_calendar.models.Event;
import com.tutoring_calendar.services.EventService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@Slf4j
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/events")
    public ResponseEntity<List<Event>> getAllEvents(){
        List<Event> events = eventService.getAllEvents();
        if(events.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/{date}")
    public ResponseEntity<EventResponse> getEventsByWeek(@PathVariable LocalDate date) {
        log.debug("Received request to get events for week with date: {}", date);

        List<Event> weekEvents = eventService.getEventsForSelectedWeek(date);
        log.debug("Retrieved events for the selected week: {}", weekEvents);

        BigDecimal currentWeekIncome = eventService.calculateCurrentIncomeForWeek(weekEvents);
        BigDecimal expectedWeekIncome = eventService.calculateExpectedIncomeForWeek(weekEvents);
        BigDecimal currentMonthIncome = eventService.calculateCurrentIncomeForMonth(date);
        BigDecimal expectedMonthIncome = eventService.calculateExpectedIncomeForMonth(date);

        log.debug("Calculated incomes - Current Week: {}, Expected Week: {}, Current Month: {}, Expected Month: {}",
                currentWeekIncome, expectedWeekIncome, currentMonthIncome, expectedMonthIncome);

        EventResponse eventResponse = new EventResponse(weekEvents, currentWeekIncome, expectedWeekIncome,
                currentMonthIncome, expectedMonthIncome);

        if (weekEvents.isEmpty()) {
            log.info("No events found for the selected week.");
            return ResponseEntity.noContent().build();
        }

        log.info("Successfully retrieved events and calculated incomes for the selected week.");
        return ResponseEntity.ok(eventResponse);
    }

    @PostMapping("/events/update")
    public ResponseEntity<Object> updateEvent(@RequestBody Event newEvent){
        log.info("Received request to update event. Event details: {}", newEvent);

        Optional<Event> updatedEvent = eventService.updateEventData(newEvent);

        return updatedEvent.map(event -> {
            Long id = event.getId();
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(id)
                    .toUri();
            log.info("Event updated successfully. Event ID: {}", id);
            return ResponseEntity.created(location).build();

        }).orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/create-event")
    public ResponseEntity<Object> createNewEvent(@RequestBody Event newEvent){
        log.info("Received request to create a new event. Event details: {}", newEvent);

        Optional<Event> createdEvent = eventService.addEvent(newEvent);

        return createdEvent.map(event -> {
            Long id = event.getId();
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(id)
                    .toUri();
            log.info("Event created successfully. Event ID: {}", id);
            return ResponseEntity.created(location).build();
        }).orElse(ResponseEntity.noContent().build());
    }

}
