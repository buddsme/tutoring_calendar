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

        String clientFullName = newEvent.getClient().getFullName();
        Optional<Client> existingClient = clientRepository.findByFullName(clientFullName);

        Client client = existingClient.orElseGet(() -> {
            Client newClient = newEvent.getClient();
            newClient.setClientStatus(ClientStatus.ACTIVE);
            newClient.setDeposit(BigDecimal.valueOf(0));
            return clientRepository.save(newClient);
        });

        newEvent.setClient(client);
        newEvent.setEventStatus(EventStatus.CREATED);

        Event event = eventRepository.save(newEvent);
        if (newEvent.getOriginalId() > 0) {
            eventRepository.save(event);
        } else {
            Long originalEventId = event.getId();
            event.setOriginalId(originalEventId);
            eventRepository.save(event);
        }

        return Optional.of(event);
    }

    public Optional<Event> updateEventData(Event newEventData) {
        Optional<Event> eventFromDB = eventRepository.findById(newEventData.getId());

        eventFromDB.ifPresent(event -> {
            event.setDate(newEventData.getDate());
            event.setRepeatable(newEventData.isRepeatable());
            event.setPrice(newEventData.getPrice());
            event.setClient(newEventData.getClient());
            event.setStartTime(newEventData.getStartTime());
            event.setFinishTime(newEventData.getFinishTime());
            event.setOriginalId(newEventData.getOriginalId());
            event.setEventStatus(EventStatus.UPDATED);
            eventRepository.save(event);
        });

        return eventFromDB;
    }

    public List<Event> getEventsForSelectedWeek(LocalDate dateOfWeek) {

        LocalDate firstDayOfSearchedWeek = dateOfWeek.with(DayOfWeek.MONDAY);
        LocalDate lastDayOfSearchedWeek = dateOfWeek.with(DayOfWeek.SUNDAY);

        createRecurringEventForSelectedWeek(dateOfWeek, firstDayOfSearchedWeek, lastDayOfSearchedWeek);

        return eventRepository.findAllByDateRange(firstDayOfSearchedWeek, lastDayOfSearchedWeek);
    }

    private void createRecurringEventForSelectedWeek(LocalDate dateOfSearchedWeek, LocalDate firstDayOfSearchedWeek, LocalDate lastDayOfSearchedWeek) {
        List<Event> originalEvents = eventRepository.findAllOriginalEvents();
        for (Event event : originalEvents) {
            boolean isDateInRange = dateOfSearchedWeek.isAfter(firstDayOfSearchedWeek) && dateOfSearchedWeek.isBefore(lastDayOfSearchedWeek);

            if (event.isRepeatable() && dateOfSearchedWeek.isAfter(event.getDate()) && !isDateInRange) {
                Optional<Event> recurringEvent = eventRepository.findRecurringEventOfSelectedWeek(event.getId(), firstDayOfSearchedWeek, lastDayOfSearchedWeek);

                if (recurringEvent.isEmpty()) {
                    DayOfWeek dayOfEvent = event.getDate().getDayOfWeek();
                    LocalDate newDateOfEvent = firstDayOfSearchedWeek.with(dayOfEvent);

                    Event newRecurrentEvent = new Event(event);
                    newRecurrentEvent.setId(-1L);
                    newRecurrentEvent.setDate(newDateOfEvent);
                    newRecurrentEvent.setOriginalId(event.getId());
                    newRecurrentEvent.setEventStatus(EventStatus.CREATED);
                    newRecurrentEvent.setRepeatable(false);
                    addEvent(newRecurrentEvent);
                }
            }
        }
    }
}