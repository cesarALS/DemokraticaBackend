package com.demokratica.backend.Repositories;

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

//Indica que vamos a ejecutar los métodos en el orden que nos indique la anotación @Order
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//Inicializa solo los componentes relacionados con la capa de repositorios, para no demorarse tanto en inicializarse y no tener
//problemas con todas las demás capas (es un unit test, después de todo)
@DataJpaTest
public class UserRepositoryTest {
    
    @Autowired
    private UsersRepository userRepository;
    @Autowired
    private AuthoritiesRepository authoritiesRepository;

    @Test
    @DisplayName("Prueba 1: guardar un usuario") //Descripción personalizada para el test
    @Order(1) //Va a ser el primer test en ejecutarse. Importante cuando los tests dependen de los anteriores
    @Rollback(value = false) //Quiere decir que una vez que se finalicen los tests no se van a deshacer los cambios en la BD
    //Guarda el usuario hernandomk junto con sus autoridades en la BD y se asegura de que el ID en la tabla authorities sea mayor a
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
