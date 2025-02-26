package com.demokratica.backend.Exceptions;

import org.springframework.security.core.Authentication;

public class UnsupportedAuthenticationException extends RuntimeException {
    
    private final String unsupportedAuthClassName;

    public UnsupportedAuthenticationException (Authentication auth) {
        super();
        this.unsupportedAuthClassName = auth.getClass().getName();
    }

    public String getMessage() {
        return "The Authentication object of class " + this.unsupportedAuthClassName + " isn't yet supported by the method that" + 
				" gets a user's email from his authentication object";
    }
}
