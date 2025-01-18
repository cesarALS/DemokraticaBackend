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
import org.springframework.web.bind.annotation.RequestParam;


//TODO: Usar el UserService en lugar del UserRepository y AuthenticationManager
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "https://demokratica.vercel.app"}, allowCredentials = "true")
public class LoginController {

    @Autowired
    private UsersRepository userRepository;
    private final AuthenticationManager authenticationManager;

    public LoginController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
    
    @PostMapping("/ingrese")
    public ResponseEntity<?> loginWithPassword(@RequestParam String email, @RequestParam String password) {
        Authentication authenticationRequest = 
            UsernamePasswordAuthenticationToken.unauthenticated(email, password);
          
        try { 
            Authentication authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>("Credenciales no válidas", HttpStatus.FORBIDDEN);
        }
        
        User user = userRepository.findById(email).orElseThrow(() -> 
                    new RuntimeException("No pudimos encontrar su nombre de usuario"));
        String username = user.getUsername();

        LoginResponse response = new LoginResponse(username, email);
        return new ResponseEntity<>(response, HttpStatus.OK);

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
}
