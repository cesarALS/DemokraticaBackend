package com.demokratica.backend.Exceptions;

public class InvitationNotFoundException extends RuntimeException {
    
    public InvitationNotFoundException (String userEmail, Long sessionId) {
        super("Couldn't find an invitation for the user with email " + userEmail + 
                " to the session with id " + String.valueOf(sessionId));
    }
}
