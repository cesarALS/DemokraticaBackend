package com.demokratica.backend.Configurations;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.demokratica.backend.Services.JWTService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public JWTService jwtService;
    public JwtAuthenticationFilter(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = authHeader.substring(7);
        try {
            jwtService.validateToken(jwtToken);
        } catch (JwtException e) {
            throw e;
        }
        
        //Macheteado solo para probar que funcione. Debería crear una clase JWTAuthentication que extienda Authentication
        //porque este tipo de autenticación no es un UsernamePassword
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(jwtService.extractEmail(jwtToken), null, AuthorityUtils.createAuthorityList("USER"));
        SecurityContext ctx = SecurityContextHolder.getContext();
        ctx.setAuthentication(authentication);
        
        //Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //authentication.setAuthenticated(true);

        filterChain.doFilter(request, response);
    }
}
