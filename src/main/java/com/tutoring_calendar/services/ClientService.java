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
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final EventRepository eventRepository;

    public ClientService(ClientRepository clientRepository, EventRepository eventRepository) {
        this.clientRepository = clientRepository;
        this.eventRepository = eventRepository;
    }

    public List<Client> getAllClients() {
        return clientRepository
                .findAll()
                .stream()
                .filter(client -> client.getClientStatus().equals(ClientStatus.ACTIVE))
                .sorted(Comparator.comparing(Client::getFullName))
                .collect(Collectors.toList());
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

    public BigDecimal countNotPaidIncome(List<Client> clients) {
        BigDecimal notPaidIncome = new BigDecimal("0");
        for (Client client : clients) {
            BigDecimal clientCurrentDeposit = client.getDeposit();

            if (clientCurrentDeposit != null && clientCurrentDeposit.compareTo(BigDecimal.valueOf(0)) < 0) {
                notPaidIncome = notPaidIncome.add(clientCurrentDeposit);
            }
        }
        return notPaidIncome;
    }

    public BigDecimal countPaidForwardIncome(List<Client> clients) {
        BigDecimal paidForwardIncome = new BigDecimal("0");
        for (Client client : clients) {
            BigDecimal clientCurrentDeposit = client.getDeposit();

            if (clientCurrentDeposit != null && clientCurrentDeposit.compareTo(BigDecimal.valueOf(0)) > 0) {
                paidForwardIncome = paidForwardIncome.add(clientCurrentDeposit);
            }
        }
        return paidForwardIncome;
    }

    public boolean updateDeposit(Long clientId, BigDecimal newDepositAmount) {
        if (clientId != null && clientRepository.existsById(clientId)) {
            Optional<Client> clientOptional = clientRepository.findById(clientId);

            return clientOptional.map(client -> {
                client.setDeposit(newDepositAmount);
                clientRepository.save(client);
                return true;
            }).orElse(false);
        }
        return false;
    }

    public boolean archiveClient(Long clientId) {
        if (clientId != null && clientRepository.existsById(clientId)) {
            Optional<Client> clientOptional = clientRepository.findById(clientId);

            return clientOptional.map(client -> {
                client.setClientStatus(ClientStatus.ARCHIVED);
                clientRepository.save(client);
                return true;
            }).orElse(false);
        }
        return false;
    }
}
