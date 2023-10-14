package com.tutoring_calendar.controllers;

import com.tutoring_calendar.dto.ClientResponse;
import com.tutoring_calendar.models.Client;
import com.tutoring_calendar.services.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/clients")
@CrossOrigin("*")
@Slf4j
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("")
    public ResponseEntity<ClientResponse> getAllClients(){
        log.info("Received request to get all clients.");

        List<Client> clients = clientService.getAllClients();
        BigDecimal notPaid = clientService.countNotPaidIncome(clients);
        BigDecimal paidForward = clientService.countPaidForwardIncome(clients);

        ClientResponse clientResponse = new ClientResponse(clients, notPaid, paidForward);

        if(clients.isEmpty()){
            log.info("No clients found.");
            return ResponseEntity.noContent().build();
        }

        log.info("Returning {} clients.", clients.size());
        return ResponseEntity.ok(clientResponse);
    }

    @PutMapping("/change-deposit")
    public ResponseEntity<Object> changeClientDeposit(@RequestParam Long clientId, @RequestParam BigDecimal newDepositAmount){
        log.info("Received request to change deposit for client with ID {}. New deposit amount: {}", clientId, newDepositAmount);

        boolean updated = clientService.updateDeposit(clientId, newDepositAmount);
        if(!updated){
            log.info("Client with ID {} not found.", clientId);
            return ResponseEntity.notFound().build();
        }

        log.info("Deposit for client with ID {} updated successfully.", clientId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/archive")
    public ResponseEntity<Object> archiveClient(@RequestParam Long clientId){
        log.info("Received request to archive client with ID {}.", clientId);

        boolean deleted = clientService.archiveClient(clientId);
        if(!deleted){
            log.info("Client with ID {} not found.", clientId);
            return ResponseEntity.notFound().build();
        }

        log.info("Client with ID {} archived successfully.", clientId);
        return ResponseEntity.noContent().build();
    }

}
