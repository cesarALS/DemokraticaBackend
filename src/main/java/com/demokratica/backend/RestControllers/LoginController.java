package com.demokratica.backend.RestControllers;

import com.demokratica.backend.Services.UserService;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


//TODO: poner los public records usados por varias clases en un lugar aparte, para centralizar y eliminar la redundancia
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "https://demokratica.vercel.app"}, allowCredentials = "true")
public class LoginController {

    private final UserService userService;
    public LoginController (UserService userService) {
        this.userService = userService;
    }
    
    //TODO: cambiar el nombre endpoint por uno que sea más RESTful 
    //TODO: recibir los parámetros como un JSON en el body
    @PostMapping("/ingrese")
    public ResponseEntity<?> loginWithPassword(@RequestParam String email, @RequestParam String password) {
        try { 
            userService.authenticateUser(email, password);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new ErrorResponse("Credenciales no válidas"), HttpStatus.FORBIDDEN);
        }
        
        String username = userService.getUsername(email);

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

    public record ErrorResponse(String error) {
    }
}
