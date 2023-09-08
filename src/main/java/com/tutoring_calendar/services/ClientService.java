package com.tutoring_calendar.services;

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
                .sorted(Comparator.comparing(Client::getFullName))
                .collect(Collectors.toList());
    }


    @Scheduled(cron = "0 1 * * * *")
    public void proceedCompletedServices() {
        List<Client> clients = getAllClients();
        for (Client client : clients) {
            List<Event> clientEvents = eventRepository.findAllByClient(client);
            for (Event event : clientEvents) {
                LocalDateTime eventFinishDateTime = event.getDate().atTime(event.getFinishTime());
                if (eventFinishDateTime.isBefore(LocalDateTime.now()) && event.getEventStatus().equals(EventStatus.CREATED)) {
                    client.setDeposit(client.getDeposit().subtract(event.getPrice()));
                    client.setServices(client.getServices() - 1);
                    event.setEventStatus(EventStatus.FINISHED);
                    eventRepository.save(event);
                    clientRepository.save(client);
                }
            }
        }
    }

    public BigDecimal countNotPaidIncome(List<Client> clients) {
        BigDecimal notPaidIncome = BigDecimal.valueOf(0);
        for(Client client : clients){
            BigDecimal clientCurrentDeposit = client.getDeposit();

            if(clientCurrentDeposit.compareTo(BigDecimal.valueOf(0)) < 0){
                notPaidIncome = notPaidIncome.add(clientCurrentDeposit);
            }
        }
        return notPaidIncome;
    }

    public BigDecimal countPaidForwardIncome(List<Client> clients) {
        BigDecimal paidForwardIncome = BigDecimal.valueOf(0);
        for(Client client : clients){
            BigDecimal clientCurrentDeposit = client.getDeposit();

            if(clientCurrentDeposit.compareTo(BigDecimal.valueOf(0)) > 0){
                paidForwardIncome = paidForwardIncome.add(clientCurrentDeposit);
            }
        }
        return paidForwardIncome;
    }

    public Optional<Client> getClientById(Long clientId) {
        if(clientId > 0){
            return clientRepository.findById(clientId);
        }
        return Optional.empty();
    }

//    public void updateDepositAndServices(Client client, BigDecimal newDepositAmount) {
//        client.setDeposit(newDepositAmount);
//        Integer services = 0;
//        if(newDepositAmount.compareTo(BigDecimal.valueOf(0)) > 0){
//            services =
//        }
//        clientRepository.save(client);
//    }
}
