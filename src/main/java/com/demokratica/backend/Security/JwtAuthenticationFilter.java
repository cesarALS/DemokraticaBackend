package com.demokratica.backend.Security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.demokratica.backend.Services.JWTService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public JWTService jwtService;
    public JwtAuthenticationProvider authProvider;
    public JwtAuthenticationFilter(JWTService jwtService, JwtAuthenticationProvider authProvider) {
        this.jwtService = jwtService;
        this.authProvider = authProvider;
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
        Authentication authRequest = JwtAuthentication.unauthenticated(jwtToken);
        Authentication authResponse = authProvider.authenticate(authRequest);

        if (authResponse != null) {
            SecurityContext newContext = SecurityContextHolder.createEmptyContext();
            newContext.setAuthentication(authResponse);
            SecurityContextHolder.setContext(newContext);
        }

        filterChain.doFilter(request, response);
    }
}
