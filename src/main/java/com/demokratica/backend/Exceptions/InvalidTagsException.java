package com.demokratica.backend.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class InvalidTagsException extends RuntimeException {
    
    public String getMessage() {
        return "Can't enter two tags with the same text";
    }

    public ResponseEntity<?> getResponse() {
        System.out.println(this.getMessage());
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
