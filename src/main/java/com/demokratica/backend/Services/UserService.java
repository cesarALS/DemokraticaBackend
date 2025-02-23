package com.demokratica.backend.Services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.demokratica.backend.Exceptions.UserAlreadyExistsException;
import com.demokratica.backend.Exceptions.UserNotFoundException;
import com.demokratica.backend.Model.Authority;
import com.demokratica.backend.Model.Plan;
import com.demokratica.backend.Model.User;

import com.demokratica.backend.Repositories.AuthoritiesRepository;
import com.demokratica.backend.Repositories.PlansRepository;
import com.demokratica.backend.Repositories.UsersRepository;
import com.demokratica.backend.RestControllers.AccountController.UserDTO;

import jakarta.transaction.Transactional;

@Service
public class UserService {
    
    private final UsersRepository userRepository;
    private final AuthoritiesRepository authoritiesRepository;
    private final PlansRepository plansRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    
    public UserService (UsersRepository userRepository, AuthoritiesRepository authoritiesRepository, PlansRepository plansRepository, PasswordEncoder encoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.authoritiesRepository = authoritiesRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.plansRepository = plansRepository;
    }

    @Transactional
    public void saveUser(String email, String username, String password) {
        if (userRepository.existsById(email)) {
            throw new UserAlreadyExistsException(email);
        }

        Plan plan = new Plan();
        plan.setPlanType(Plan.Type.GRATUITO);
        //NOTA: al no settear la fecha de expiracion el sistema asumirá que esta es null, que es lo que queremos en este caso
        plansRepository.save(plan);

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setEnabled(true);
        user.setPlan(plan);
        userRepository.save(user);

        Authority authority = new Authority();
        authority.setAuthority("USER");
        authority.setUser(user);
        authoritiesRepository.save(authority);
    }

    @Transactional
    public void deleteUser(String email) {
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
    public void updateUsername(String email, String newUsername) {
        User user = userRepository.findById(email).orElseThrow(() ->
            new UserNotFoundException(email));

        user.setUsername(newUsername);
        userRepository.save(user);
    }

    public void authenticateUser(String email, String password) {
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(email, password);
        Authentication authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);
        SecurityContextHolder.getContext().setAuthentication(authenticationResponse);
    }

    public String getUsername(String email) {
        User user = userRepository.findById(email).orElseThrow(() ->
                new UserNotFoundException(email));

        return user.getUsername();
    }

    public boolean existsById(String email) {
        return userRepository.existsById(email);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = users.stream().map(user -> {
            return new UserDTO(user.getUsername(), user.getEmail());
        }).collect(Collectors.toList());

        return userDTOs;
    }
}
