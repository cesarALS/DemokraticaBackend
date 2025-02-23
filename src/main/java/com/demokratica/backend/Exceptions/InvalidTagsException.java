package com.demokratica.backend.Exceptions;

public class InvalidTagsException extends RuntimeException {
    
    public String getMessage() {
        return "Can't enter two tags with the same text";
    }
}
