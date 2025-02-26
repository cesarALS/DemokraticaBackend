package com.demokratica.backend.Exceptions;

public class PollNotFoundException extends RuntimeException {
    
    public PollNotFoundException (Long id) {
        super("Couldn't find poll with id " + String.valueOf(id));
    }
}
