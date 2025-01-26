package com.demokratica.backend.RestControllers;

import org.springframework.web.bind.annotation.RestController;

import com.demokratica.backend.Services.JWTService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



/*
 * Las responsabilidades de esta clase son certificar que un JWT es válido (porque para ello hay que verificar
 * su firma digital) y extraer los atributos del payload para facilitarle esta labor al cliente (aunque él mismo lo
 * podría hacer)
 */
@RestController
public class JWTController {
    
    JWTService jwtService;
    public JWTController(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/token-info")
    public ResponseEntity<TokenInfo> getTokenInfo(@RequestBody Token token) {
        String jwtToken = token.jwtToken();
        String email = jwtService.extractEmail(jwtToken);
        String username = jwtService.extractUsername(jwtToken);
        //TODO: extraer las authorities de manera correcta
        String authority = "USER";
        Boolean isValid = jwtService.validateToken(jwtToken);
        TokenInfo tokenInfo;
        if (isValid) {
            tokenInfo = new TokenInfo(isValid, email, username, authority);
        } else {
            tokenInfo = new TokenInfo(false, null, null, null);
        }

        return new ResponseEntity<>(tokenInfo, HttpStatus.OK);
    }

    @PostMapping("/validar_token")
    public ResponseEntity<?> validateJWT(@RequestBody Token token) {
        String jwtToken = token.jwtToken();
        boolean isValid = jwtService.validateToken(jwtToken);
        return new ResponseEntity<>(isValid, HttpStatus.OK);
    }
    
    //TODO: retornar las authorities de manera más general
    public record TokenInfo(boolean isValid, String email, String username, String authority) {
    }

    public record Token(String jwtToken) {
    }
}
