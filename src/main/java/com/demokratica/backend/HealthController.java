package com.demokratica.backend;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*
    Este controlador lo definí solo para asegurarme de que la aplicación funciona correctamente
    y es accesible. Al enviar una petición GET al endpoint /health/ me devuelve un OK
    TODO: eliminar esta clase
 */
@RestController
public class HealthController {
    
    @GetMapping("/health/")
    public ResponseEntity<?> checkHealth() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
