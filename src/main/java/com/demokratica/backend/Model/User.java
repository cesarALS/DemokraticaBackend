package com.demokratica.backend.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    //Este atributo lo pide Spring Security para guardar usuarios
    private Boolean enabled;

    @Enumerated(EnumType.STRING) //Especificamos que se guarde como String y no como número
    private Plan plan;
    enum Plan {GRATUITO, PLUS, PREMIUM, PROFESIONAL};
    private LocalDateTime expirationDate; //Puede ser null en el caso de lplan gratuito
    
}
