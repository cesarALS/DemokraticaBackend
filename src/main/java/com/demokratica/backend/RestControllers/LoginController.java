package com.demokratica.backend.RestControllers;

import com.demokratica.backend.Services.JWTService;
import com.demokratica.backend.Services.UserService;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//TODO: poner los public records usados por varias clases en un lugar aparte, para centralizar y eliminar la redundancia
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "https://demokratica.vercel.app"}, allowCredentials = "true")
public class LoginController {

    private final UserService userService;
    private final JWTService jwtService;
    public LoginController (UserService userService, JWTService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }
    
    @PostMapping("/api/auth/login")
    public ResponseEntity<?> loginWithPassword(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.email();
        String password = loginRequest.password();
        try { 
            userService.authenticateUser(email, password);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new ErrorResponse("Credenciales no válidas"), HttpStatus.FORBIDDEN);
        }
        
        String username = userService.getUsername(email);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String jwtToken = jwtService.buildToken(authentication, userService);
        LoginResponse response = new LoginResponse(username, email, jwtToken);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    public record LoginRequest(String email, String password) {
    }

    public record LoginResponse(String username, String email, String jwtToken) {
    }

    public record ErrorResponse(String error) {
    }
}
