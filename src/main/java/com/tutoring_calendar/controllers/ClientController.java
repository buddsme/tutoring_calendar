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
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/clients")
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

//    @PostMapping("/clients/change-deposit")
//    public ResponseEntity<Object> changeClientDeposit(@RequestParam Long clientId, @RequestParam BigDecimal newDepositAmount){
//        Optional<Client> client = clientService.getClientById(clientId);
//
//        return client.map(c->{
//            clientService.updateDepositAndServices(c, newDepositAmount);
//
//            return ResponseEntity.noContent().build();
//        }).orElse(ResponseEntity.ok().build());
//    }




}
