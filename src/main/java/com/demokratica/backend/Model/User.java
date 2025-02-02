package com.demokratica.backend.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data 
public class User {

    //NOTA: los atributos DEBEN SER PRIVADOS y se deben establecer Getters y Setters. En nuestro caso esto
    //lo hace Lombok gracias a la anotación @Data
    @Id
    private String email;
    private String username;
    private String password;
    private Boolean enabled;
}
