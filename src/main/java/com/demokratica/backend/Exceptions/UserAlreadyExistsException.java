package com.demokratica.backend.Exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException (String email) {
        super("El correo " + email + " ya está asociado a una cuenta");
    }
}
