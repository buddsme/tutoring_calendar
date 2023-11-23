package com.tutoring_calendar.integration.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tutoring_calendar.TutoringCalendarApplication;
import com.tutoring_calendar.dto.EventResponse;
import com.tutoring_calendar.dto.EventUpdateDTO;
import com.tutoring_calendar.enums.EventStatus;
import com.tutoring_calendar.models.Client;
import com.tutoring_calendar.models.Event;
import com.tutoring_calendar.repositories.ClientRepository;
import com.tutoring_calendar.repositories.EventRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = TutoringCalendarApplication.class
)
@AutoConfigureMockMvc
public class EventControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private ClientRepository clientRepository;

    @Test
    void givenListOfEvents_whenGetAllEvents_thenStatus200() throws Exception {

        List<Event> expected = eventRepository.findAll();

        MvcResult mvcResult = mvc.perform(get("/events")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        List<Event> actual = objectMapper.readValue(content, new TypeReference<List<Event>>() {
        });

        assertThat(content).contains("Artem Denysiuk");
        assertThat(content).contains("Max Stryzheus");

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @Transactional
    @Rollback(true)
    void givenEmptyListOfEvents_whenGetAllEvents_thenStatus204() throws Exception {

        eventRepository.deleteAll();

        MvcResult mvcResult = mvc.perform(get("/events")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(print())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        assertThat(content).isEmpty();
    }

    @Test
    void givenPassedDate_whenGetEventsByWeek_thenStatus200() throws Exception {

        // Retrieve existing events and clients
        Event event1 = eventRepository.findById(4L).orElseThrow(() -> new AssertionError("Event Not Found"));
        Event event2 = eventRepository.findById(6L).orElseThrow(() -> new AssertionError("Event Not Found"));
        Client clientWithId1 = clientRepository.findById(1L).orElseThrow(() -> new AssertionError("Client Not Found"));
        Client clientWithId3 = clientRepository.findById(3L).orElseThrow(() -> new AssertionError("Client Not Found"));

        // Create new recurrent events and save them
        Event recurrentEvent3 = createEvent(clientWithId1, BigDecimal.valueOf(200), LocalDate.of(2023, 10, 22), EventStatus.CREATED, 1L);
        Event recurrentEvent4 = createEvent(clientWithId3, BigDecimal.valueOf(200), LocalDate.of(2023, 10, 22), EventStatus.CREATED, 3L);
        saveEvent(recurrentEvent3);
        saveEvent(recurrentEvent4);

        // Retrieve saved recurrent events
        Event event3 = eventRepository.findById(7L).orElseThrow(() -> new AssertionError("Event Not Found"));
        Event event4 = eventRepository.findById(8L).orElseThrow(() -> new AssertionError("Event Not Found"));

        // Prepare expected events
        List<Event> expectedEvents = List.of(event1, event2, event3, event4);

        // Perform the API call
        LocalDate date = LocalDate.of(2023, 10, 19);
        MvcResult mvcResult = mvc.perform(get("/events/{date}", date).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // Extract and deserialize the response
        String content = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        EventResponse actualResponse = objectMapper.readValue(content, EventResponse.class);
        List<Event> actualEvents = actualResponse.getEvents();

        // Assert the response
        assertThat(actualEvents).containsExactlyInAnyOrderElementsOf(expectedEvents);
    }

    private Event createEvent(Client client, BigDecimal price, LocalDate date, EventStatus status, Long originalId) {
        return new Event(null, client, price, date, LocalTime.of(13, 0), LocalTime.of(14, 0), false, status, originalId);
    }

    private void saveEvent(Event event) {
        eventRepository.save(event);
    }

    @Test
    void givenNewEventData_whenUpdateEvent_thenStatus204() throws Exception {
        EventUpdateDTO newEventData = new EventUpdateDTO(1L, null, BigDecimal.valueOf(400), null, LocalTime.of(18, 0),
                LocalTime.of(19, 0), true, null, null);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String eventJson = objectMapper.writeValueAsString(newEventData);

        mvc.perform(post("/events/update")
                        .contentType(APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", "http://localhost/events/update/1"));

        Event event = eventRepository.findById(1L).orElseThrow(() -> new AssertionError("Event not found"));

        assertThat(event.getStartTime()).hasHour(18).hasMinute(0);
        assertThat(event.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(400));
    }

    @Test
    public void givenEmptyEventData_whenUpdateEvent_thenStatus404() throws Exception {
        EventUpdateDTO emptyEventUpdateDTO = new EventUpdateDTO();

        ObjectMapper objectMapper = new ObjectMapper();
        String eventJson = objectMapper.writeValueAsString(emptyEventUpdateDTO);

        mvc.perform(post("/events/update")
                        .contentType(APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void givenNewEventData_whenCreateEvent_thenStatus201() throws Exception {
        Client existedClient = clientRepository.findById(1L).orElseThrow(() -> new AssertionError("Client not found"));
        Event newEvent = new Event(null,
                existedClient,
                BigDecimal.valueOf(250),
                LocalDate.of(2023, 10, 24),
                LocalTime.of(18, 0), LocalTime.of(19, 0),
                true,
                null,
                -1L);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String eventJson = objectMapper.writeValueAsString(newEvent);

        mvc.perform(post("/events/create-event")
                        .contentType(APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated());
    }
}
