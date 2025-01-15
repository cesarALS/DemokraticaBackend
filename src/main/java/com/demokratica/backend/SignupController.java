package com.demokratica.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "https://demokratica.vercel.app"}, allowCredentials = "true")
public class SignupController {
    
    @Autowired
    private UsersRepository userRepository;
    @Autowired
    private AuthoritiesRepository authoritiesRepository;
    @Autowired
    private PasswordEncoder encoder;

    @PostMapping("/unase")
    @Transactional
    public ResponseEntity<?> signUp(@RequestParam String email, @RequestParam String username, @RequestParam String password) {
        //Esta lógica debería ir en una clase aparte de tipo @Service

        if (userRepository.existsById(email)) {
            ErrorResponse e = new ErrorResponse("Correo ya utilizado", 
                        "El correo que ingresó ya está asociado a una cuenta");
            return new ResponseEntity<>(e, HttpStatus.CONFLICT);
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

        SignupResponse response = new SignupResponse(username, email);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    

    public record ErrorResponse (String error, String response) {
    }

    public record SignupResponse (String username, String email) {
    }
}
