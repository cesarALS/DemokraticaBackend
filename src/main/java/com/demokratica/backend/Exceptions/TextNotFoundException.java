package com.demokratica.backend.Exceptions;

public class TextNotFoundException extends RuntimeException {
    
    public TextNotFoundException(Long textId) {
        super("Couldn't find text with id " + textId);
    }
}
