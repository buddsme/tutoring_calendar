package com.tutoring_calendar.services;

import com.tutoring_calendar.models.Client;
import com.tutoring_calendar.models.Event;
import com.tutoring_calendar.repositories.ClientRepository;
import com.tutoring_calendar.repositories.EventRepository;
import org.springframework.stereotype.Service;

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
        return Optional.of(savedEvent);
    }

    public List<Event> getAllEvents(){
        return eventRepository.findAll();
    }
}
