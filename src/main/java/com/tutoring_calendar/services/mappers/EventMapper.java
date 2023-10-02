package com.tutoring_calendar.services.mappers;

import com.tutoring_calendar.dto.EventUpdateDTO;
import com.tutoring_calendar.models.Event;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {
    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);


    Event populateEventWithPresentEventUpdateDTOFields(@MappingTarget Event event, EventUpdateDTO eventDTO);
}

