package com.demokratica.backend.RestControllers;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RestController;

import com.demokratica.backend.Exceptions.UnsupportedAuthenticationException;
import com.demokratica.backend.Exceptions.UserAlreadyExistsException;
import com.demokratica.backend.Services.JWTService;
import com.demokratica.backend.Services.UserService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//TODO: pasar los public records de la clase que sean comunes a otras a un lugar distinto
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "https://demokratica.vercel.app"}, allowCredentials = "true")
public class SignupController {
    
    private UserService userService;
    private UserDetailsService userDetailsService;
    private JWTService jwtService;
    public SignupController (UserService userService, JWTService jwtService, UserDetailsService userDetailsService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    //TODO: cambiar el nombre del endpoint por uno más RESTful
    @PostMapping("/unase")
    public ResponseEntity<?> signUp(@RequestBody SignupData signupData) {
        try {
            userService.saveUser(signupData.email(), signupData.username(), signupData.password());
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
        }
        //TODO: es posible que esta parte sea redundante y que Spring Security ya lo esté haciendo él mismo
        //Le decimos a Spring Security que un usuario recién registrado ya está autenticado y que usó el método de usuario y contraseña
        //Alternativamente podríamos hacer la autenticación después de crear el JWT y decirle que se autenticó usando un JWT, pero estaríamos mintiendo
        UserDetails userDetails = userDetailsService.loadUserByUsername(signupData.email());
        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(userDetails, null, 
                                    new ArrayList<>(AuthorityUtils.createAuthorityList("USER")));
        SecurityContext newContext = SecurityContextHolder.createEmptyContext();
        newContext.setAuthentication(auth);
        SecurityContextHolder.setContext(newContext);

        try {
            String jwtToken = jwtService.buildToken(auth, userService);
            SignupResponse response = new SignupResponse(signupData.username(), signupData.email(), jwtToken);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (UnsupportedAuthenticationException e) {
            return e.getResponse();
        }
    }
    

    public record ErrorResponse (String error) {
    }

    public record SignupResponse (String username, String email, String jwtToken) {
    }

    public record SignupData (String email, String username, String password) {
    }
}
