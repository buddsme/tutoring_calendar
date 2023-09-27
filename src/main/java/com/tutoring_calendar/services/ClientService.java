package com.tutoring_calendar.services;

import com.tutoring_calendar.enums.ClientStatus;
import com.tutoring_calendar.models.Client;
import com.tutoring_calendar.models.Event;
import com.tutoring_calendar.repositories.ClientRepository;
import com.tutoring_calendar.repositories.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;
    private final EventRepository eventRepository;

    public ClientService(ClientRepository clientRepository, EventRepository eventRepository) {
        this.clientRepository = clientRepository;
        this.eventRepository = eventRepository;
    }

    public List<Client> getAllClients() {
        log.info("Getting all active clients and sorting them by full name.");

        List<Client> activeClients = clientRepository
                .findAll()
                .stream()
                .filter(client -> client.getClientStatus().equals(ClientStatus.ACTIVE))
                .sorted(Comparator.comparing(Client::getFullName))
                .collect(Collectors.toList());

        log.debug("Retrieved {} active clients.", activeClients.size());
        return activeClients;
    }

    public BigDecimal countNotPaidIncome(List<Client> clients) {
        log.info("Calculating total not paid income.");

        BigDecimal notPaidIncome = new BigDecimal("0");
        for (Client client : clients) {
            BigDecimal clientCurrentDeposit = client.getDeposit();

            if (clientCurrentDeposit != null && clientCurrentDeposit.compareTo(BigDecimal.valueOf(0)) < 0) {
                notPaidIncome = notPaidIncome.add(clientCurrentDeposit);
            }
        }

        log.debug("Total not paid income calculated: {}", notPaidIncome);
        return notPaidIncome;
    }

    public BigDecimal countPaidForwardIncome(List<Client> clients) {
        log.info("Calculating total paid forward income.");

        BigDecimal paidForwardIncome = new BigDecimal("0");
        for (Client client : clients) {
            BigDecimal clientCurrentDeposit = client.getDeposit();

            if (clientCurrentDeposit != null && clientCurrentDeposit.compareTo(BigDecimal.valueOf(0)) > 0) {
                paidForwardIncome = paidForwardIncome.add(clientCurrentDeposit);
            }
        }

        log.debug("Total paid forward income calculated: {}", paidForwardIncome);
        return paidForwardIncome;
    }

    public boolean updateDeposit(Long clientId, BigDecimal newDepositAmount) {
        log.debug("Updating deposit for client with ID: {}", clientId);

        if (clientId != null && clientRepository.existsById(clientId)) {
            Optional<Client> clientOptional = clientRepository.findById(clientId);

            return clientOptional.map(client -> {
                client.setDeposit(newDepositAmount);
                clientRepository.save(client);
                return true;
            }).orElse(false);
        }

        log.warn("Failed to update deposit. Client with ID {} does not exist.", clientId);
        return false;
    }

    public boolean archiveClient(Long clientId) {
        log.debug("Archiving client with ID: {}", clientId);

        if (clientId == null || !clientRepository.existsById(clientId)) {
            log.warn("Failed to archive client. Client with ID {} does not exist.", clientId);
            return false;
        }

        Optional<Client> clientOptional = clientRepository.findById(clientId);
        clientOptional.ifPresent(client -> {
            client.setClientStatus(ClientStatus.ARCHIVED);
            stopRepeatClientServices(client);
            clientRepository.save(client);
        });

        log.debug("Client with ID {} has been archived.", clientId);
        return clientOptional.isPresent();
    }

    private void stopRepeatClientServices(Client client) {
        log.debug("Stopping repeat services for client with ID: {}", client.getId());

        List<Event> clientEvents = eventRepository.findAllByClient(client);
        for (Event event : clientEvents) {
            if (event.isRepeatable()) {
                event.setRepeatable(false);
                eventRepository.save(event);
            }
        }

        log.debug("Stopped repeat services for client with ID: {}", client.getId());
    }
}
