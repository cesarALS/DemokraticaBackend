package com.demokratica.backend.Model;

import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<Authority> authorities;

    @Enumerated(EnumType.STRING)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @OneToMany(mappedBy= "invitedUser", cascade = CascadeType.ALL)
    private Set<Invitation> invitations;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserVote> votes;
    
}
