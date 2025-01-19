package com.demokratica.backend.Exceptions;

public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException (String email) {
        super("No se pudo encontrar al usuario con correo " + email);
    }
}
