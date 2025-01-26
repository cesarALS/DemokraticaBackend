package com.demokratica.backend.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;

//TODO: entender cómo funciona todo esto y dejar de usar métodos deprecados
@Service
public class JWTService {

    //TODO: crear una llave secreta buena y asegurarla mediante environment variables o algún otro método
    private static final String SECRET_KEY = "vM7VZknE5lS1kT7F7sErmZQ6aH27A7XfYvVNoF1B3l412"; 
    private static final long expirationTime = 10 * 60 * 1000; //10 min en milisegundos: 10 min * 60 seg por minuto * 1000 ms por segundo

    //Por ahora que la autenticación es solo con correo y contraseña la lógica es más sencilla, pero cuando también pueda ser por
    //OAuth se volverá más compleja
    public String buildToken(Authentication authentication, UserService userService) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        String username = userService.getUsername(email);
        return internalBuildToken(email, username);
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

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    //TODO: hacer una validación de verdad, teniendo en cuenta tiempos de expiración y la firma digital
    public boolean validateToken(String token, UserService userService) {
        Claims claims = extractAllClaims(token);
        Date expirationDate = claims.getExpiration();
        boolean isExpired = expirationDate.before(new Date());

        String email = extractEmail(token);
        return !isExpired && userService.existsById(email);
    }
}
