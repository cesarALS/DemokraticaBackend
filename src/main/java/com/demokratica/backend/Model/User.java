package com.demokratica.backend.Model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data 
public class User {
    @Id
    private String email;
    private String username;
    private String password;
    //Este atributo lo pide Spring Security para guardar usuarios
    private Boolean enabled;

    @Enumerated(EnumType.STRING)
    @OneToOne
    private Plan plan;

    @OneToMany(mappedBy= "invitedUser", cascade = CascadeType.ALL)
    private List<Invitation> invitations;
    
}
