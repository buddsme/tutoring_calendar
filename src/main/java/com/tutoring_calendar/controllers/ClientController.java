package com.tutoring_calendar.controllers;

import com.tutoring_calendar.dto.ClientResponse;
import com.tutoring_calendar.models.Client;
import com.tutoring_calendar.services.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/clients")
@CrossOrigin("*")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("")
    public ResponseEntity<ClientResponse> getAllClients(){
        List<Client> clients = clientService.getAllClients();
        BigDecimal notPaid = clientService.countNotPaidIncome(clients);
        BigDecimal paidForward = clientService.countPaidForwardIncome(clients);

        ClientResponse clientResponse = new ClientResponse(clients, notPaid, paidForward);

        if(clients.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientResponse);
    }

    @PutMapping("/change-deposit")
    public ResponseEntity<Object> changeClientDeposit(@RequestParam Long clientId, @RequestParam BigDecimal newDepositAmount){

        boolean updated = clientService.updateDeposit(clientId, newDepositAmount);
        if(!updated){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/archive")
    public ResponseEntity<Object> archiveClient(@RequestParam Long clientId){
        boolean deleted = clientService.archiveClient(clientId);
        if(!deleted){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

}
