package com.demokratica.backend;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;



@RestController
@CrossOrigin(origins = {"http://localhost:3000", "https://demokratica.vercel.app"}, allowCredentials = "true", allowedHeaders = {"Content-Type",
"Authorization", "X-Requested-With"}, methods = {RequestMethod.POST, RequestMethod.OPTIONS})
public class LoginController {

    @Autowired
    private UsersRepository userRepository;
    private final AuthenticationManager authenticationManager;

    public LoginController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
    
    @PostMapping("/ingrese")
    public ResponseEntity<?> loginWithPassword(@RequestBody Credentials credentials) {
        Authentication authenticationRequest = 
            UsernamePasswordAuthenticationToken.unauthenticated(credentials.email(), credentials.password());
          
        try { 
            Authentication authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);
        } catch (BadCredentialsException e) {
            ErrorResponse errorResponse = new ErrorResponse("Credenciales no válidas", "Correo o contraseña incorrectos");
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }
        
        User user = userRepository.findById(credentials.email()).orElseThrow(() -> 
                    new RuntimeException("No pudimos encontrar su nombre de usuario"));
        String username = user.getUsername();

        LoginResponse loginResponse = new LoginResponse(username, credentials.email());
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);

    }
    
    /*
        Un record es una nueva funcionalidad de Java introducida en la versión 14
        Básicamente es una manera mucho más rápida y eficiente de declarar clases con las
        siguientes características
            -Sus atributos son de tipo private final (solo accesibles directamente por la clase y una vez que
            se inicializan en el constructor no pueden volver a ser cambiados, es decir, son INMUTABLES)
            -Tiene Getters para que otras clases puedan ver el valor de esos atributos
            -Tiene un constructor público que toma como argumento los dos valores que declaramos aquí
        Es decir, nos ahorra el trabajo de tener que escribir manualmente el código para hacer todo eso
        Para verlo más un poco más en detalle mirar https://www.baeldung.com/java-record-keyword
    */
    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(String username, String email) {
    }

    public record Credentials(String email, String password) {
    }

    public record ErrorResponse(String error, String description) {
    }
}
