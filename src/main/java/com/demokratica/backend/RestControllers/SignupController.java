package com.demokratica.backend.RestControllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.demokratica.backend.Exceptions.UserAlreadyExistsException;
import com.demokratica.backend.Services.UserService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

//TODO: pasar los public records de la clase que sean comunes a otras a un lugar distinto
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "https://demokratica.vercel.app"}, allowCredentials = "true")
public class SignupController {
    
    @Autowired
    private UserService userService;

    //TODO: cambiar el nombre del endpoint por uno más RESTful
    //TODO: recibir los parámetros como un JSON dentro del body
    @PostMapping("/unase")
    @Transactional
    public ResponseEntity<?> signUp(@RequestParam String email, @RequestParam String username, @RequestParam String password) {
        try {
            userService.saveUser(email, username, password);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
        }

        SignupResponse response = new SignupResponse(username, email);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    

    public record ErrorResponse (String error) {
    }

    public record SignupResponse (String username, String email) {
    }
}
