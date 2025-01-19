package com.demokratica.backend.Services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.demokratica.backend.Exceptions.UserAlreadyExistsException;
import com.demokratica.backend.Exceptions.UserNotFoundException;
import com.demokratica.backend.Model.Authority;
import com.demokratica.backend.Model.User;

import com.demokratica.backend.Repositories.AuthoritiesRepository;
import com.demokratica.backend.Repositories.UsersRepository;

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
        if (userRepository.existsById(email)) {
            throw new UserAlreadyExistsException(email);
        }

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

    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findById(email).orElseThrow(() ->
                new UserNotFoundException(email));
        
        Long id = authoritiesRepository.findByUser(user).getId();

        authoritiesRepository.deleteById(id);
        userRepository.deleteById(email);
    }

    @Transactional
    public void updatePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findById(email).orElseThrow(() ->
            new UserNotFoundException(email));
        
        authenticateUser(email, currentPassword);
    
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void updateUsername(String email, String newUsername, String password) {
        User user = userRepository.findById(email).orElseThrow(() ->
            new UserNotFoundException(email));

        authenticateUser(email, password);

        user.setUsername(newUsername);
        userRepository.save(user);
    }

    public void authenticateUser(String email, String password) {
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(email, password);
        this.authenticationManager.authenticate(authenticationRequest);
    }

    public String getUsername(String email) {
        User user = userRepository.findById(email).orElseThrow(() ->
                new UserNotFoundException(email));

        return user.getUsername();
    }
    

}
