package com.demokratica.backend.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.demokratica.backend.Exceptions.UnsupportedAuthenticationException;
import com.demokratica.backend.Security.SecurityConfig;

import java.util.Date;
import java.util.HashMap;

//TODO: entender cómo funciona todo esto y dejar de usar métodos deprecados
@Service
public class JWTService {

    //TODO: crear una llave secreta buena y asegurarla mediante environment variables o algún otro método
    private static final String SECRET_KEY = "vM7VZknE5lS1kT7F7sErmZQ6aH27A7XfYvVNoF1B3l412"; 
    private static final long expirationTime = 7 * 24* 60 * 60 * 1000; //7 días en milisegundos: 7 días * 24 horas * 60 min * 60 seg por minuto * 1000 ms por segundo

    @Autowired
    private UserService userService;

    //Por ahora que la autenticación es solo con correo y contraseña la lógica es más sencilla, pero cuando también pueda ser por
    //OAuth se volverá más compleja
    public String buildToken(Authentication authentication, UserService userService) throws UnsupportedAuthenticationException {
        try {
            String email = SecurityConfig.getUsernameFromAuthentication();
            String username = userService.getUsername(email);
            return internalBuildToken(email, username);
        } catch (UnsupportedAuthenticationException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    //TODO: volverlo este método private y hacer el testing con el método buildToken
    public String internalBuildToken(String email, String username) {
        HashMap<String, String> extraClaims = new HashMap<>();
        //Poner el correo en el extraClaims no es necesario porque viene en la sección del JWT llamada subject
        extraClaims.put("username", username);
        extraClaims.put("authorities", "USER");

        return Jwts.builder()
                .claims(extraClaims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getPayload();
    }

    public static String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public static String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        if (!claims.containsKey("username")) {
            throw new RuntimeException("The JWT token " + token + " doesn't contain a username");
        }

        return (String) claims.get("username");
    }

    //TODO: hacer una validación de verdad, teniendo en cuenta tiempos de expiración y la firma digital
    public boolean validateToken(String token) {
        //Asumo que si se pudo ejecutar el método extractAllClaims es porque no se lanzó ninguna excepción de que el token no fuera válido
        try {
            extractAllClaims(token);
            String tokenUsername = extractUsername(token);
            String email = extractEmail(token);
            String databaseUsername = userService.getUsername(email);
            if (!tokenUsername.equals(databaseUsername)) {
                throw new JwtException("El username del token no coincide con el de la BD");
            }
        } catch (JwtException j) {
            return false;
        }
        
        return true;
    }
}
