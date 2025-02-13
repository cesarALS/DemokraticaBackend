package com.demokratica.backend.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public class UnsupportedAuthenticationException extends Exception {
    
    private String unsupportedAuthClassName;

    public UnsupportedAuthenticationException (Authentication auth) {
        super();
        this.unsupportedAuthClassName = auth.getClass().getName();
    }

    public String getMessage() {
        return "The Authentication object of class " + this.unsupportedAuthClassName + " isn't supported by the method that" + 
				" gets a user's email from his authentication object";
    }

    //Este método es llamado cada vez que ocurre la excepción y el lugar donde se produjo la excepción es un controlador que
    //quiere retornar el código de respuesta en este caso fácilmente.
    //Este fragmento de código se repetía mucho y quería tenerlo en un solo lugar para facilitar el cambio o deshabilitación
    //del logging y poder cambiar el código del error fácilmente
    public ResponseEntity<?> getResponse() {
        //Log the exception so that we are aware of in on the backend
        System.out.println(this.getMessage());
        //Return info in the response body so that the frontend is aware of it too
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
