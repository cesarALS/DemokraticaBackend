package com.demokratica.backend;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class AccountController {
    
    private UserService userService;
    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/actualizar_contraseña")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordChange passwordChange) {
        /*
         * Estoy asumiendo que en el formulario para actualizar la contraseña pide la contraseña actual por razones
         * de seguridad (para que si deja la cuenta abierta otro no pueda llegar a cambiarle la contraseña)
         * Primero hay que validar que la contraseña sea la correcta
         * Luego sí se hace la actualización
         */
        try {
            userService.updatePassword(passwordChange.email(), passwordChange.currentPassword(), passwordChange.newPassword());
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>("Credenciales no válidas", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
         
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public record PasswordChange (String email, String currentPassword, String newPassword) {
    }
    
}
