package com.tutoring_calendar.services;

import com.tutoring_calendar.models.Client;
import com.tutoring_calendar.models.Event;
import com.tutoring_calendar.repositories.ClientRepository;
import com.tutoring_calendar.repositories.EventRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ClientRepository clientRepository;

    public EventService(EventRepository eventRepository, ClientRepository clientRepository) {
        this.eventRepository = eventRepository;
        this.clientRepository = clientRepository;
    }

    public Optional<Event> addEvent(Event newEvent) {
        String clientFullName = newEvent.getClient().getFullName();
        Optional<Client> existingClient = clientRepository.findByFullName(clientFullName);

        Client client = existingClient.orElseGet(() -> clientRepository.save(newEvent.getClient()));

        newEvent.setClient(client);

        Event savedEvent = eventRepository.save(newEvent);

        if(newEvent.isRepeatable()){
            repeatEventEveryWeek(newEvent, client);
        }
        return Optional.of(savedEvent);
    }

    private void repeatEventEveryWeek(Event newEvent, Client client) {
        LocalDate startDate = newEvent.getDate().plusWeeks(1); // Initial start date
        LocalDate endDate = startDate.plusWeeks(5); // Calculate end date

        while (startDate.isBefore(endDate)) {
            Event repeatableEvent = new Event();
            repeatableEvent.setClient(client);
            repeatableEvent.setPrice(newEvent.getPrice());
            repeatableEvent.setStartTime(newEvent.getStartTime());
            repeatableEvent.setFinishTime(newEvent.getFinishTime());
            repeatableEvent.setRepeatable(true);
            repeatableEvent.setDate(startDate);

            eventRepository.save(repeatableEvent);

            startDate = startDate.plusWeeks(1);
        }
    }

    public List<Event> getAllEvents(){
        return eventRepository.findAll();
    }

    public List<Event> getEventsForCurrentWeek(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        LocalDate startOfTheWeek = date;

        if(dayOfWeek.getValue() > 1){
            startOfTheWeek = date.minusDays(dayOfWeek.getValue() - 1);
        }

        LocalDate endOfTheWeek = startOfTheWeek.plusDays(6);

        return eventRepository.findAllByWeekRange(startOfTheWeek, endOfTheWeek);
    }
}
