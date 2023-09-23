package com.tutoring_calendar.services;

import com.tutoring_calendar.enums.ClientStatus;
import com.tutoring_calendar.enums.EventStatus;
import com.tutoring_calendar.models.Client;
import com.tutoring_calendar.models.Event;
import com.tutoring_calendar.repositories.ClientRepository;
import com.tutoring_calendar.repositories.EventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public BigDecimal calculateCurrentIncomeForWeek(List<Event> weekEvents) {

        BigDecimal income = new BigDecimal("0");

        for (Event event : weekEvents) {
            if (event.getDate().isBefore(LocalDate.now())) {
                income = income.add(event.getPrice());
            }
        }
        return income;
    }

    public BigDecimal calculateExpectedIncomeForWeek(List<Event> weekEvents) {
        BigDecimal income = new BigDecimal("0");

        for (Event event : weekEvents) {
            income = income.add(event.getPrice());
        }
        return income;
    }

    public BigDecimal calculateCurrentIncomeForMonth(LocalDate date) {
        LocalDate firstDateOfMonth = date.withDayOfMonth(1);

        YearMonth yearMonth = YearMonth.from(date);
        LocalDate lastDateOfMonth = yearMonth.atEndOfMonth();
        List<Event> monthEvents = eventRepository.findAllByDateRange(firstDateOfMonth, lastDateOfMonth);

        BigDecimal income = new BigDecimal("0");
        for (Event event : monthEvents) {
            if (event.getDate().isBefore(LocalDate.now())) {
                income = income.add(event.getPrice());
            }
        }
        return income;
    }

    public BigDecimal calculateExpectedIncomeForMonth(LocalDate date) {
        LocalDate firstDateOfMonth = date.withDayOfMonth(1);

        YearMonth yearMonth = YearMonth.from(date);
        LocalDate lastDateOfMonth = yearMonth.atEndOfMonth();
        List<Event> monthEvents = eventRepository.findAllByDateRange(firstDateOfMonth, lastDateOfMonth);

        BigDecimal income = new BigDecimal("0");
        for (Event event : monthEvents) {
            income = income.add(event.getPrice());
        }
        return income;

    }

    @Scheduled(cron = "0 1 * * * *")
    public void proceedCompletedEvents() {
        List<Client> clients = clientRepository.findAll();
        for (Client client : clients) {
            processClientEvents(client);
        }
    }

    private void processClientEvents(Client client) {
        List<Event> clientEvents = eventRepository.findAllByClient(client);
        LocalDateTime currentDateTime = LocalDateTime.now();

        for (Event event : clientEvents) {
            LocalDateTime eventFinishDateTime = event.getDate().atTime(event.getFinishTime());

            if (eventFinishDateTime.isBefore(currentDateTime) && event.getEventStatus().equals(EventStatus.CREATED)) {
                updateClientAndEvent(client, event);
            }
        }
    }

    private void updateClientAndEvent(Client client, Event event) {
        client.setDeposit(client.getDeposit().subtract(event.getPrice()));
        event.setEventStatus(EventStatus.FINISHED);
        eventRepository.save(event);
        clientRepository.save(client);
    }


    public Optional<Event> addEvent(Event newEvent) {
        Client client = getOrCreateClient(newEvent.getClient());
        newEvent.setClient(client);
        newEvent.setEventStatus(EventStatus.CREATED);

        Event event = eventRepository.save(newEvent);

        if (newEvent.getOriginalId() <= 0) {
            Long originalEventId = event.getId();
            event.setOriginalId(originalEventId);
            eventRepository.save(event);
        }

        return Optional.of(event);
    }

    private Client getOrCreateClient(Client client) {
        String clientFullName = client.getFullName();
        return clientRepository.findByFullName(clientFullName).orElseGet(() -> {
            client.setClientStatus(ClientStatus.ACTIVE);
            client.setDeposit(BigDecimal.valueOf(0));
            return clientRepository.save(client);
        });
    }

    public Optional<Event> updateEventData(Event updatedEventData) {
        Optional<Event> eventOptional = eventRepository.findById(updatedEventData.getId());

        eventOptional.ifPresent(event -> {
            updateEventFields(event, updatedEventData);
            event.setEventStatus(EventStatus.UPDATED);
            eventRepository.save(event);
        });

        return eventOptional;
    }

    private void updateEventFields(Event event, Event updatedEvent) {
        event.setDate(updatedEvent.getDate());
        event.setRepeatable(updatedEvent.isRepeatable());
        event.setPrice(updatedEvent.getPrice());
        event.setClient(updatedEvent.getClient());
        event.setStartTime(updatedEvent.getStartTime());
        event.setFinishTime(updatedEvent.getFinishTime());
        event.setOriginalId(updatedEvent.getOriginalId());
    }

    public List<Event> getEventsForSelectedWeek(LocalDate dateOfWeek) {

        LocalDate firstDayOfSearchedWeek = dateOfWeek.with(DayOfWeek.MONDAY);
        LocalDate lastDayOfSearchedWeek = dateOfWeek.with(DayOfWeek.SUNDAY);

        createRecurringEventsForWeek(dateOfWeek, firstDayOfSearchedWeek, lastDayOfSearchedWeek);

        return eventRepository.findAllByDateRange(firstDayOfSearchedWeek, lastDayOfSearchedWeek);
    }

    private void createRecurringEventsForWeek(LocalDate targetDate, LocalDate startOfWeek, LocalDate endOfWeek) {
        List<Event> originalEvents = eventRepository.findAllOriginalEvents();

        for (Event event : originalEvents) {
            if (eventShouldBeRecreated(event, targetDate, startOfWeek, endOfWeek)) {
                Optional<Event> recurringEvent = eventRepository.findRecurringEventOfSelectedWeek(
                        event.getId(), startOfWeek, endOfWeek);

                if (recurringEvent.isEmpty()) {
                    LocalDate newDateOfEvent = calculateNewEventDate(event.getDate(), startOfWeek);

                    Event newRecurrentEvent = createRecurrentEvent(event, newDateOfEvent);
                    addEvent(newRecurrentEvent);
                }
            }
        }
    }

    private boolean eventShouldBeRecreated(Event event, LocalDate targetDate, LocalDate startOfWeek, LocalDate endOfWeek) {
        LocalDate eventDate = event.getDate();
        return event.isRepeatable() && eventDate.isBefore(targetDate) && !isDateInRange(targetDate, startOfWeek, endOfWeek);
    }

    private boolean isDateInRange(LocalDate date, LocalDate startOfWeek, LocalDate endOfWeek) {
        return !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek);
    }

    private LocalDate calculateNewEventDate(LocalDate originalDate, LocalDate startOfWeek) {
        DayOfWeek dayOfEvent = originalDate.getDayOfWeek();
        return startOfWeek.with(dayOfEvent);
    }

    private Event createRecurrentEvent(Event originalEvent, LocalDate newDate) {
        Event newRecurrentEvent = new Event(originalEvent);
        newRecurrentEvent.setId(-1L);
        newRecurrentEvent.setDate(newDate);
        newRecurrentEvent.setOriginalId(originalEvent.getId());
        newRecurrentEvent.setEventStatus(EventStatus.CREATED);
        newRecurrentEvent.setRepeatable(false);
        return newRecurrentEvent;
    }
}