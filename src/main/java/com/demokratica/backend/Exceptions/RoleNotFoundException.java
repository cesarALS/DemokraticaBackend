package com.demokratica.backend.Exceptions;

public class RoleNotFoundException extends RuntimeException {
    
    public RoleNotFoundException (String userEmail, long sessionID) {
        super("Couldn't find the role of user with email " + userEmail + " in session with id " + String.valueOf(sessionID));
    }
}
