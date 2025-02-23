package com.demokratica.backend.Exceptions;

public class SessionNotFoundException extends RuntimeException {
    
    public SessionNotFoundException (long sessionId) {
        super("Couldn't find session with id " + sessionId);
    }


}
