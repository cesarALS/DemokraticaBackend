package com.demokratica.backend.Security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import com.demokratica.backend.Services.JWTService;

public class JwtAuthentication implements Authentication {

    private String jwtToken;
    private List<GrantedAuthority> authorities;
    private boolean isAuthenticated;
    private String principal;
    
    private JwtAuthentication(String jwtToken, List<GrantedAuthority> authorities, boolean isAuthenticated) {
        this.jwtToken = jwtToken;
        this.authorities = authorities;
        this.isAuthenticated = isAuthenticated;
        this.principal = JWTService.extractEmail(jwtToken);
    }

    public static JwtAuthentication unauthenticated(String jwtToken) {
        return new JwtAuthentication(jwtToken, Collections.emptyList(), false);
    }

    public static JwtAuthentication authenticated(String jwtToken) {
        return new JwtAuthentication(jwtToken, AuthorityUtils.createAuthorityList("USER"), true);
    }

    @Override
    public String getName() {
        return "JwtAuthentication";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public Object getCredentials() {
        return this.jwtToken;
    }

    @Override
    public Object getDetails() {
        //Retornamos null  porque no hay detalles adicionales que queramos devolver
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public boolean isAuthenticated() {
        return this.isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
    }
    
}
