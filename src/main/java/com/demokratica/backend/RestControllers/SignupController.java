package com.demokratica.backend.RestControllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.demokratica.backend.Exceptions.UserAlreadyExistsException;
import com.demokratica.backend.Services.UserService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//TODO: pasar los public records de la clase que sean comunes a otras a un lugar distinto
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "https://demokratica.vercel.app"}, allowCredentials = "true")
public class SignupController {
    
    private UserService userService;
    public SignupController (UserService userService) {
        this.userService = userService;
    }

    //TODO: cambiar el nombre del endpoint por uno más RESTful
    @PostMapping("/unase")
    public ResponseEntity<?> signUp(@RequestBody SignupData signupData) {
        try {
            userService.saveUser(signupData.email(), signupData.username(), signupData.password());
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
        }

        SignupResponse response = new SignupResponse(signupData.username(), signupData.email());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    

    public record ErrorResponse (String error) {
    }

    public record SignupResponse (String username, String email) {
    }

    public record SignupData (String email, String username, String password) {
    }
}
