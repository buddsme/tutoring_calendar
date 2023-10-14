package com.tutoring_calendar.integration.controllers;

import com.tutoring_calendar.TutoringCalendarApplication;
import com.tutoring_calendar.enums.ClientStatus;
import com.tutoring_calendar.models.Client;
import com.tutoring_calendar.repositories.ClientRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = TutoringCalendarApplication.class
)
@AutoConfigureMockMvc
class ClientControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ClientRepository clientRepository;

    @Test
    void givenClients_whenGetClients_thenStatus200() throws Exception {
        mvc.perform(get("/clients")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clients.length()").value(2))
                .andExpect(jsonPath("$.clients[0].fullName").value("Artem Denysiuk"))
                .andExpect(jsonPath("$.clients[1].fullName").value("Vlad Pozniak"))
                .andExpect(jsonPath("$.notPaid").value(BigDecimal.valueOf(0)))
                .andExpect(jsonPath("$.paidForward").value(BigDecimal.valueOf(0)));
    }

    @Test
    @Transactional
    @Rollback(value = true)
    void givenNoClients_whenGetClients_thenStatus204() throws Exception {

        clientRepository.deleteAll();

        mvc.perform(get("/clients")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.clients").doesNotExist())
                .andExpect(jsonPath("$.notPaid").doesNotExist())
                .andExpect(jsonPath("$.paidForward").doesNotExist());
    }

    @Test
    void givenExistedClientIdAndNewDepositAmount_whenChangeClientDeposit_thenStatus204() throws Exception {
        mvc.perform(put("/clients/change-deposit")
                        .param("clientId", "1")
                        .param("newDepositAmount", "1000.00"))
                .andExpect(status().isNoContent());

        Optional<Client> updatedClientOptional = clientRepository.findById(1L);

        assertTrue(updatedClientOptional.isPresent());

        Client updatedClient = updatedClientOptional.orElseThrow(() -> new AssertionError("Updated client with ID 1 not found"));

        assertThat(updatedClient.getDeposit())
                .isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void givenNonExistedClientIdAndNewDepositAmount_whenChangeClientDeposit_thenStatus404() throws Exception {
        mvc.perform(put("/clients/change-deposit")
                        .param("clientId", "25")
                        .param("newDepositAmount", "1000.00"))
                .andExpect(status().isNotFound());

        Optional<Client> updatedClientOptional = clientRepository.findById(25L);

        assertTrue(updatedClientOptional.isEmpty());
    }

    @Test
    @Transactional
    @Rollback(value = true)
    void givenExistedClientId_whenArchiveClient_thenStatus204() throws Exception {
        mvc.perform(put("/clients/archive")
                        .param("clientId", "1"))
                .andExpect(status().isNoContent());

        Optional<Client> updatedClientOptional = clientRepository.findById(1L);

        assertTrue(updatedClientOptional.isPresent());

        Client updatedClient = updatedClientOptional.orElseThrow(() -> new AssertionError("Updated client with ID 1 not found"));

        assertThat(updatedClient.getClientStatus()).isEqualByComparingTo(ClientStatus.ARCHIVED);
    }

    @Test
    void givenNonExistedClientId_whenArchiveClient_thenStatus404() throws Exception {
        mvc.perform(put("/clients/archive")
                        .param("clientId", "44"))
                .andExpect(status().isNotFound());

        Optional<Client> updatedClientOptional = clientRepository.findById(44L);

        assertTrue(updatedClientOptional.isEmpty());
    }
}