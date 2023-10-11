package com.tutoring_calendar.models;

import com.tutoring_calendar.enums.ClientStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "clients")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    @NotNull(message = "Full name is mandatory")
    private String fullName;

    @Column(name = "deposit")
    private BigDecimal deposit;

    @Column(name = "status")
    private ClientStatus clientStatus;
}
