package com.demokratica.backend.RestControllers;

import com.demokratica.backend.Exceptions.UnsupportedAuthenticationException;
import com.demokratica.backend.Exceptions.UserNotFoundException;
import com.demokratica.backend.Services.JWTService;
import com.demokratica.backend.Services.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
public class AccountController {
    
    private UserService userService;
    private JWTService jwtService;
    public AccountController(UserService userService, JWTService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PutMapping("/api/users/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable String userEmail, @RequestBody PasswordChange passwordChange) {
        /*
         * Estoy asumiendo que en el formulario para actualizar la contraseña pide la contraseña actual por razones
         * de seguridad (para que si deja la cuenta abierta otro no pueda llegar a cambiarle la contraseña)
         * Primero hay que validar que la contraseña sea la correcta, que es algo que updatePassword hace en el fondo
         * Luego sí se hace la actualización
         */
        try {
            userService.updatePassword(userEmail, passwordChange.currentPassword(), passwordChange.newPassword());
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
         
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/api/users/{email}")
    public ResponseEntity<?> deleteAccount (@PathVariable String email, @RequestBody PasswordRequest request) {
        try {
            userService.authenticateUser(email, request.password());
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }

        userService.deleteUser(email);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/api/users/{email}/username")
    public ResponseEntity<?> updateUsername(@PathVariable String email, @RequestBody UsernameChange usernameChange) {
        try {
            userService.updateUsername(email, usernameChange.newUsername());
        }
        //TODO: esta lógica se repite demasiado y habría que abstraerla de alguna forma
        catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //Al hacer un cambio de nombre de usuario hay que actualizar el JWT porque de lo contrario dejará de ser válido
        try  {
            String jwtToken = jwtService.buildToken(auth, userService);
            return new ResponseEntity<>(new JWT(jwtToken), HttpStatus.OK);
        } catch (UnsupportedAuthenticationException e) {
            return e.getResponse();
        }
        
    }

    @GetMapping("/api/users")
    public ResponseEntity<?> emailIsTaken(@RequestBody EmailDTO dto) {
        Map<String, Boolean> response = new HashMap<>();
        if (userService.existsById(dto.email())) {
            response.put("exists", true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("exists", false);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //Este endpoint se usa para buscar participantes al crear sesiones. Múltiples páginas como Instagram y
    //Facebook permiten buscar cuáles son sus usuarios activos cuando uno realiza una búsqueda
    @GetMapping("/api/users")
    public ResponseEntity<?> returnAllUsers() {
        List<UserDTO> userDTOs = userService.getAllUsers();
        return new ResponseEntity<>(userDTOs, HttpStatus.OK);
    }

    public record UserDTO (String username, String email) {
    }    

    public record EmailDTO (String email) {

    }

    public record PasswordChange (String currentPassword, String newPassword) {
    }

    public record UsernameChange (String newUsername) {
    }

    public record JWT (String jwtToken) {
    }

    public record ErrorResponse (String error) {
    }

    public record PasswordRequest (String password) {
    }
}
