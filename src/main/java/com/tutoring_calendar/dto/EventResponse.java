package com.tutoring_calendar.dto;

import com.tutoring_calendar.models.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {
    private List<Event> events;
    private BigDecimal currentWeekIncome;
    private BigDecimal expectedWeekIncome;
    private BigDecimal currentMonthIncome;
    private BigDecimal expectedMonthIncome;
}
