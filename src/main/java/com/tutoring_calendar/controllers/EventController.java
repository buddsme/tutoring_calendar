package com.tutoring_calendar.controllers;

import com.tutoring_calendar.dto.EventResponse;
import com.tutoring_calendar.models.Event;
import com.tutoring_calendar.services.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
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
    public ResponseEntity<EventResponse> getEventsByWeek(@PathVariable LocalDate date){
        List<Event> weekEvents = eventService.getEventsForCurrentWeek(date);
        BigDecimal currentWeekIncome = eventService.calculateCurrentIncomeForWeek(weekEvents);
        BigDecimal expectedWeekIncome = eventService.calculateExpectedIncomeForWeek(weekEvents);
        BigDecimal currentMonthIncome = eventService.calculateCurrentIncomeForMonth(date);
        BigDecimal expectedMonthIncome = eventService.calculateExpectedIncomeForMonth(date);

        EventResponse eventResponse = new EventResponse(weekEvents, currentWeekIncome, expectedWeekIncome, currentMonthIncome, expectedMonthIncome);

        if(weekEvents.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(eventResponse);
    }

    @PostMapping("/create-event")
    public ResponseEntity<Object> createNewEvent(@RequestBody Event newEvent){

        Optional<Event> event = eventService.addEvent(newEvent);

        return event.map(e -> {
            Long id = e.getId();
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(id)
                    .toUri();
            return ResponseEntity.created(location).build();
        }).orElse(ResponseEntity.noContent().build());
    }

}
