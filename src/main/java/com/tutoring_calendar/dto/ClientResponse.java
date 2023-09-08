package com.tutoring_calendar.dto;

import com.tutoring_calendar.models.Client;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class ClientResponse {
    private List<Client> events;
    private BigDecimal notPaid;
    private BigDecimal paidForward;
}
