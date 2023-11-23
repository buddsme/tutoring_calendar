package com.tutoring_calendar.services;

import com.tutoring_calendar.dto.EventResponse;
import com.tutoring_calendar.dto.EventUpdateDTO;
import com.tutoring_calendar.enums.ClientStatus;
import com.tutoring_calendar.enums.EventStatus;
import com.tutoring_calendar.exceptions.EventNotFoundException;
import com.tutoring_calendar.models.Client;
import com.tutoring_calendar.models.Event;
import com.tutoring_calendar.repositories.ClientRepository;
import com.tutoring_calendar.repositories.EventRepository;
import com.tutoring_calendar.services.mappers.EventMapper;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final ClientRepository clientRepository;

    public EventService(EventRepository eventRepository, ClientRepository clientRepository) {
        this.eventRepository = eventRepository;
        this.clientRepository = clientRepository;
    }

    public List<Event> getAllEvents() {
        log.info("Retrieving all events.");
        return eventRepository.findAll();
    }

    public BigDecimal calculateCurrentIncomeForWeek(List<Event> weekEvents) {
        log.info("Calculating current income for the week.");

        BigDecimal income = new BigDecimal("0");

        for (Event event : weekEvents) {
            if (event.getDate().isBefore(LocalDate.now())) {
                income = income.add(event.getPrice());
            }
        }
        log.debug("Current income for the week calculated: {}", income);
        return income;
    }

    public BigDecimal calculateExpectedIncomeForWeek(List<Event> weekEvents) {
        log.info("Calculating expected income for the week.");

        BigDecimal income = new BigDecimal("0");

        for (Event event : weekEvents) {
            income = income.add(event.getPrice());
        }
        log.debug("Expected income for the week calculated: {}", income);
        return income;
    }

    public BigDecimal calculateCurrentIncomeForMonth(LocalDate date) {
        log.info("Calculating current income for the month.");

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
        log.debug("Current income for the month calculated: {}", income);
        return income;
    }

    public BigDecimal calculateExpectedIncomeForMonth(LocalDate date) {
        log.info("Calculating expected income for the month.");

        LocalDate firstDateOfMonth = date.withDayOfMonth(1);
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate lastDateOfMonth = yearMonth.atEndOfMonth();
        List<Event> monthEvents = eventRepository.findAllByDateRange(firstDateOfMonth, lastDateOfMonth);

        BigDecimal income = new BigDecimal("0");
        for (Event event : monthEvents) {
            income = income.add(event.getPrice());
        }
        log.debug("Expected income for the month calculated: {}", income);
        return income;
    }

    @Scheduled(cron = "0 1 * * * *")
    public void proceedCompletedEvents() {
        log.info("Starting to proceed completed events.");

        List<Client> clients = clientRepository.findAll();
        for (Client client : clients) {
            processClientEvents(client);
        }

        log.info("Completed proceeding all clients' events.");
    }

    private void processClientEvents(Client client) {
        log.debug("Processing events for client with ID {}.", client.getId());

        List<Event> clientEvents = eventRepository.findAllByClient(client);
        LocalDateTime currentDateTime = LocalDateTime.now();

        for (Event event : clientEvents) {
            LocalDateTime eventFinishDateTime = event.getDate().atTime(event.getFinishTime());

            if (eventFinishDateTime.isBefore(currentDateTime) && event.getEventStatus().equals(EventStatus.CREATED)) {
                updateClientAndEvent(client, event);
            }
        }

        log.debug("Finished processing events for client with ID {}.", client.getId());
    }

    private void updateClientAndEvent(Client client, Event event) {
        log.debug("Updating client and event for event ID {}.", event.getId());

        client.setDeposit(client.getDeposit().subtract(event.getPrice()));
        event.setEventStatus(EventStatus.FINISHED);
        eventRepository.save(event);
        clientRepository.save(client);

        log.debug("Client and event updated for event ID {}.", event.getId());
    }

    public Optional<Event> addEvent(Event newEvent) {
        log.info("Adding a new event.");

        Client client = getOrCreateClient(newEvent.getClient());
        newEvent.setClient(client);
        newEvent.setEventStatus(EventStatus.CREATED);

        Event event = eventRepository.save(newEvent);

        if (newEvent.getOriginalId() <= 0) {
            Long originalEventId = event.getId();
            event.setOriginalId(originalEventId);
            eventRepository.save(event);
        }

        log.debug("New event added with ID {}.", event.getId());
        return Optional.of(event);
    }

    private Client getOrCreateClient(Client client) {
        log.debug("Getting or creating client with full name: {}", client.getFullName());

        String clientFullName = client.getFullName();
        return clientRepository.findByFullName(clientFullName).orElseGet(() -> {
            client.setClientStatus(ClientStatus.ACTIVE);
            client.setDeposit(BigDecimal.valueOf(0));
            Client createdClient = clientRepository.save(client);
            log.debug("New client created with ID {}.", createdClient.getId());
            return createdClient;
        });
    }

    public Optional<Event> updateEventData(EventUpdateDTO updatedEventData) {

        if(updatedEventData.getId() == null){
            return Optional.empty();
        }

        log.debug("Updating event data for event ID: {}", updatedEventData.getId());

        Optional<Event> eventOptional = eventRepository.findById(updatedEventData.getId());

        Event savedEvent = eventOptional.orElseThrow(() -> new EventNotFoundException("Event not found in database"));

        savedEvent = EventMapper.INSTANCE.populateEventWithPresentEventUpdateDTOFields(savedEvent, updatedEventData);

        Event updatedEvent = eventRepository.save(savedEvent);

        log.debug("Event data updated for event ID: {}", updatedEventData.getId());
        return Optional.of(updatedEvent);
    }

    public EventResponse getEventsForSelectedWeek(LocalDate dateOfWeek) {

        log.debug("Retrieving events for the selected week starting from: {}", dateOfWeek);

        LocalDate firstDayOfSearchedWeek = dateOfWeek.with(DayOfWeek.MONDAY);
        LocalDate lastDayOfSearchedWeek = dateOfWeek.with(DayOfWeek.SUNDAY);

        createRecurringEventsForWeek(dateOfWeek, firstDayOfSearchedWeek, lastDayOfSearchedWeek);

        List<Event> events = eventRepository.findAllByDateRange(firstDayOfSearchedWeek, lastDayOfSearchedWeek);
        log.debug("Retrieved {} events for the selected week.", events.size());

        BigDecimal currentWeekIncome = calculateCurrentIncomeForWeek(events);
        BigDecimal expectedWeekIncome = calculateExpectedIncomeForWeek(events);
        BigDecimal currentMonthIncome = calculateCurrentIncomeForMonth(dateOfWeek);
        BigDecimal expectedMonthIncome = calculateExpectedIncomeForMonth(dateOfWeek);

        log.debug("Calculated incomes - Current Week: {}, Expected Week: {}, Current Month: {}, Expected Month: {}",
                currentWeekIncome, expectedWeekIncome, currentMonthIncome, expectedMonthIncome);

        return new EventResponse(events, currentWeekIncome, expectedWeekIncome,
                currentMonthIncome, expectedMonthIncome);
    }

    private void createRecurringEventsForWeek(LocalDate targetDate, LocalDate startOfWeek, LocalDate endOfWeek) {
        log.debug("Creating recurring events for the week starting from: {}", startOfWeek);

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

        log.info("Recurring events for the week created.");
    }

    private boolean eventShouldBeRecreated(Event event, LocalDate targetDate, LocalDate startOfWeek, LocalDate endOfWeek) {
        log.debug("Checking if event should be recreated for event ID: {}", event.getId());

        LocalDate eventDate = event.getDate();
        boolean shouldBeRecreated = event.isRepeatable() && eventDate.isBefore(targetDate) && isDateInRange(targetDate, startOfWeek, endOfWeek);

        log.debug("Date should{} be recreated for event ID: {}", shouldBeRecreated ? "" : " not", event.getId());
        return shouldBeRecreated;
    }

    private boolean isDateInRange(LocalDate date, LocalDate startOfWeek, LocalDate endOfWeek) {
        log.trace("Checking if date {} is in range [{}, {}]", date, startOfWeek, endOfWeek);

        boolean isInRange = date.isAfter(startOfWeek) && date.isBefore(endOfWeek);

        log.trace("Date {} is in range [{}, {}]: {}", date, startOfWeek, endOfWeek, isInRange);
        return isInRange;
    }

    private LocalDate calculateNewEventDate(LocalDate originalDate, LocalDate startOfWeek) {
        log.debug("Calculating new event date for original event with date {} and start of the week {}", originalDate, startOfWeek);

        DayOfWeek dayOfEvent = originalDate.getDayOfWeek();
        LocalDate newEventDate = startOfWeek.with(dayOfEvent);

        log.debug("Calculated new event date: {}", newEventDate);
        return newEventDate;
    }

    private Event createRecurrentEvent(Event originalEvent, LocalDate newDate) {
        log.debug("Creating recurrent event for original event ID: {}", originalEvent.getId());

        Event newRecurrentEvent = new Event(originalEvent);
        newRecurrentEvent.setId(-1L);
        newRecurrentEvent.setDate(newDate);
        newRecurrentEvent.setOriginalId(originalEvent.getId());
        newRecurrentEvent.setEventStatus(EventStatus.CREATED);
        newRecurrentEvent.setRepeatable(false);

        log.debug("Recurrent event created for original event ID: {}", originalEvent.getId());
        return newRecurrentEvent;
    }
}