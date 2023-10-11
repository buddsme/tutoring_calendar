package com.tutoring_calendar.controllers;

import com.tutoring_calendar.exceptions.EventNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({EventNotFoundException.class})
    public ResponseEntity<Object> handleInternalServerExceptions(Exception ex){
        return ResponseEntity.internalServerError().body(ex.getMessage());
    }
}
