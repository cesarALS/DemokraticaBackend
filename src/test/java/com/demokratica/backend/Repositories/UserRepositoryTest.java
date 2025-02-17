/*package com.demokratica.backend.Repositories;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.demokratica.backend.Model.Authority;
import com.demokratica.backend.Model.User;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DataJpaTest
public class UserRepositoryTest {
    
    @Autowired
    private UsersRepository userRepository;
    @Autowired
    private AuthoritiesRepository authoritiesRepository;

    @Test
    @DisplayName("Prueba 1: guardar un usuario")
    @Order(1) 
    @Rollback(value = false) 
    //TODO: cambiar el hernandomk por otros valores si en algún momento hay que presentar esto
    public void saveUserTest() {
        User user = new User();
        String id = "hernandomk@gmail.com";
        user.setEmail(id);
        user.setUsername("hernandomk");
        user.setPassword("MentalidadDeTiburon");
        user.setEnabled(false);

        userRepository.save(user);

        Authority authority = new Authority();
        authority.setAuthority("PROFE");
        authority.setUser(user);

        authoritiesRepository.save(authority);

        System.out.println(user);
        System.out.println(authority);

        Assertions.assertThat(authoritiesRepository.findByUser(user).getId()).isGreaterThan(0);
    }
}
*/