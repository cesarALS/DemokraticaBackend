package com.demokratica.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class SignupController {
    
    @Autowired
    private UsersRepository userRepository;
    @Autowired
    private AuthoritiesRepository authoritiesRepository;
    @Autowired
    private PasswordEncoder encoder;

    @PostMapping("/unase")
    @Transactional
    public ResponseEntity<?> signUp(@RequestParam String username, @RequestParam String password) {
        //Esta lógica debería ir en una clase aparte de tipo @Service

        //TODO: añadir la lógica que verifica que no exista alguien con el mismo username
        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setEnabled(true);
        userRepository.save(user);
        System.out.println("Successfully saved data to the users table");

        Authority authority = new Authority();
        authority.setAuthority("USER");
        authority.setUser(user);
        authoritiesRepository.save(authority);
        System.out.println("Succesfully saved data to the authorities table");

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    
}
