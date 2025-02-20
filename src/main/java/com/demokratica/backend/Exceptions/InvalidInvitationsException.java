package com.demokratica.backend.Exceptions;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class InvalidInvitationsException extends RuntimeException {
    
    public enum Type {INVITED_OWNER, INVITED_TWICE_DIFF_ROLE, INVITED_TWICE, INVITED_ADDITIONAL_OWNER}
    private Type errorType;
    private final HashMap<Type, String> message = new HashMap<>(){{
        put(Type.INVITED_OWNER, "the owner can't be invited to his own session");
        put(Type.INVITED_TWICE_DIFF_ROLE, "the same user can't be invited twice with a different role");
        put(Type.INVITED_TWICE, "The same user can't be invited twice, even with the same role");
        put(Type.INVITED_ADDITIONAL_OWNER, "You can't invite someone as owner. There can only be one owner");
    }};

    public InvalidInvitationsException(Type errorType) {
        this.errorType = errorType;
    }
    public String getMessage() {
        return "Invalid invitation list: " + message.get(this.errorType);
    }

    public ResponseEntity<?> getResponse() {
        System.out.println(this.getMessage());
        return new ResponseEntity<>(this.getMessage(), HttpStatus.BAD_REQUEST);
    }

    public Type getErrorType() {
        return this.errorType;
    }
}
