package com.demokratica.backend.Services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;

/*
 * TODO: Las responsabilidades de este servicio son crear los tokens con un principal, autoridades
 * y tiempo de expiración dados.
 * En el principal incluir tanto correo electrónico como nombre de usuario porque el frontend necesita ambos
 * También es responsable de codificar los tokens y decodificarlos
 */

//TODO: entender cómo funciona todo esto y dejar de usar métodos deprecados
@Service
public class JWTService {

    //TODO: crear una llave secreta buena y asegurarla mediante environment variables o algún otro método
    private static final String SECRET_KEY = "vM7VZknE5lS1kT7F7sErmZQ6aH27A7XfYvVNoF1B3l412"; // You should load this securely from a properties file or env

    //TODO: adaptar todo el código a lo que se necesita para Spring: authorities, correo en nuestro caso, etc.
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                //TODO: reducir el tiempo de expiración de los tokens a 15 min
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        return !getUsernameFromToken(token).isEmpty();
    }
}
