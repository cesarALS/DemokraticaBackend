package com.demokratica.backend.Security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.demokratica.backend.Services.JWTService;

import io.jsonwebtoken.JwtException;

public class JwtAuthenticationProvider implements AuthenticationProvider {
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication authRequest = (JwtAuthentication) authentication;
        String jwtToken = (String) authRequest.getCredentials();
        try {
            JWTService.validateToken(jwtToken);
        } catch (JwtException j) {
            return null;
        }

        return JwtAuthentication.authenticated(jwtToken);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }
    
}
