package com.demokratica.backend.Exceptions;

import java.time.LocalDateTime;

public class InvalidTimesException extends RuntimeException {
    
    public InvalidTimesException (LocalDateTime start, LocalDateTime end) {
        super("The start time " + start.toString() + " comes after the end time " 
                + end.toString() + ", which is wrong");   
    }
}
