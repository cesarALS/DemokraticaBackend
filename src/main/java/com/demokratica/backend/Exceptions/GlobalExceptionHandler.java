package com.demokratica.backend.Exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UnsupportedAuthenticationException.class)
    public ResponseEntity<?> handleUnsupportedAuthenticationException (UnsupportedAuthenticationException ex) {
        //501 es NOT_IMPLEMENTED, porque no hemos implementado el soporte a los sistemas de autenticación
        //que causan están excepción
        return handleExceptionsHelper(ex, 501);
    }

    @ExceptionHandler(InvalidInvitationsException.class)
    public ResponseEntity<?> handleInvalidInvitationsException (InvalidInvitationsException ex) {
        return handleExceptionsHelper(ex, 400);
    }

    @ExceptionHandler(InvalidTagsException.class)
    public ResponseEntity<?> handleInvalidTagsException (InvalidTagsException ex) {
        return handleExceptionsHelper(ex, 400);
    }

    private ResponseEntity<?> handleExceptionsHelper (Exception ex, int httpStatus) {
        System.out.println(ex.getMessage());
        return ResponseEntity.status(httpStatus).body(ex.getMessage());
    }

}
