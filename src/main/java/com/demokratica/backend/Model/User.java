package com.demokratica.backend.Model;

import java.util.List;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

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
@Audited
@Table(name = "users")
@Data 
public class User {
    @Id
    private String email;
    private String username;
    private String password;
    //Este atributo lo pide Spring Security para guardar usuarios
    private Boolean enabled;

    @NotAudited
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)    
    List<Authority> authorities;

    @Enumerated(EnumType.STRING)
    @NotAudited
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @NotAudited
    @OneToMany(mappedBy= "invitedUser", cascade = CascadeType.ALL)
    private List<Invitation> invitations;

    @NotAudited
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserVote> votes;

    @NotAudited
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserWord> words;
    
}
