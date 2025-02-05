package com.demokratica.backend.RestControllers;

import org.springframework.web.bind.annotation.RestController;

import com.demokratica.backend.Services.JWTService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * Las responsabilidades de esta clase son certificar que un JWT es válido (porque para ello hay que verificar
 * su firma digital) y extraer los atributos del payload para facilitarle esta labor al cliente (aunque él mismo lo
 * podría hacer)
 */
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "https://demokratica.vercel.app"}, allowCredentials = "true")
public class JWTController {

    @Autowired
    private JWTService jwtService;
    
    @GetMapping("/token-info")
    public ResponseEntity<?> getTokenInfo(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String jwtToken = authHeader.substring(7);
        
        String email = JWTService.extractEmail(jwtToken);
        String username = JWTService.extractUsername(jwtToken);
        //TODO: extraer las authorities de manera correcta
        String authority = "USER";
        Boolean isValid = jwtService.validateToken(jwtToken);
        TokenInfo tokenInfo;
        if (isValid) {
            tokenInfo = new TokenInfo(isValid, email, username, authority);
        } else {
            return new ResponseEntity<>("Token is not valid", HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(tokenInfo, HttpStatus.OK);
    }
    
    //TODO: retornar las authorities de manera más general
    public record TokenInfo(boolean isValid, String email, String username, String authority) {
    }
}
