package com.demokratica.backend.RestControllers;

import com.demokratica.backend.Exceptions.UserNotFoundException;
import com.demokratica.backend.Services.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;



@RestController
public class AccountController {
    
    private UserService userService;
    public AccountController(UserService userService) {
        this.userService = userService;
    }

    //TODO: cambiar el nombre del endpoint por uno que sea más RESTful
    @PostMapping("/actualizar_contraseña")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordChange passwordChange) {
        /*
         * Estoy asumiendo que en el formulario para actualizar la contraseña pide la contraseña actual por razones
         * de seguridad (para que si deja la cuenta abierta otro no pueda llegar a cambiarle la contraseña)
         * Primero hay que validar que la contraseña sea la correcta, que es algo que updatePassword hace en el fondo
         * Luego sí se hace la actualización
         * Las excepciones que pueden ocurrir son que no exista un usuario con ese correo o que las credenciales ingresadas
         * no sean válidas (y por lo tanto no está autorizado para cambiar la contraseña)
         */
        try {
            userService.updatePassword(passwordChange.email(), passwordChange.currentPassword(), passwordChange.newPassword());
        } catch (UserNotFoundException e) {
            ErrorResponse error = new ErrorResponse(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        } catch (BadCredentialsException e) {
            ErrorResponse error = new ErrorResponse("Credenciales no válidas");
            return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
         
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("users/{email}")
    public ResponseEntity<?> deleteAccount (@PathVariable String email, @RequestBody PasswordRequest request) {
        try {
            userService.authenticateUser(email, request.password());
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }

        userService.deleteUser(email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/users/{email}")
    public ResponseEntity<?> updateUsernamne(@PathVariable String email, @RequestBody UsernameChange usernameChange) {
        try {
            userService.updateUsername(email, usernameChange.newUsername(), usernameChange.password());
        }
        //TODO: esta lógica se repite demasiado y habría que abstraerla de alguna forma
        catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
        
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public record PasswordChange (String email, String currentPassword, String newPassword) {
    }

    public record UsernameChange (String newUsername, String password) {
    }

    public record ErrorResponse (String error) {
    }

    public record PasswordRequest (String password) {
    }
}
