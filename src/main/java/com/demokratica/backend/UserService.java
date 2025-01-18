package com.demokratica.backend;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class UserService {
    
    private final UsersRepository userRepository;
    private final AuthoritiesRepository authoritiesRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    
    public UserService (UsersRepository userRepository, AuthoritiesRepository authoritiesRepository, PasswordEncoder encoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.authoritiesRepository = authoritiesRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public void saveUser(String email, String username, String password) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setEnabled(true);
        userRepository.save(user);

        Authority authority = new Authority();
        authority.setAuthority("USER");
        authority.setUser(user);
        authoritiesRepository.save(authority);
    }

    public boolean existsById(String email) {
        return userRepository.existsById(email);
    }

    @Transactional
    public void updatePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findById(email).orElseThrow(() ->
            new RuntimeException("No se pudo encontrar un usuario con el correo " + email));
        
        if (!this.authenticateUser(email, currentPassword)) {
            throw new BadCredentialsException("Credenciales no válidas");
        }
        
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean authenticateUser(String email, String password) {
        Authentication authenticationRequest = 
            UsernamePasswordAuthenticationToken.unauthenticated(email, password);
          
        try { 
            Authentication authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);
        } catch (BadCredentialsException e) {
            return false;
        }
        

        return true;
    }
    

}
