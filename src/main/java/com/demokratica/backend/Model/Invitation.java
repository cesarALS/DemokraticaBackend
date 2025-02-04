package com.demokratica.backend.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "invitations")
@Data
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_email")
    private User invitedUser;
    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;

    @Enumerated(EnumType.STRING)
    private Role role;
    public enum Role {DUEÑO, ADMIN, EDITOR, PARTICIPANTE};

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_status")
    private InvitationStatus status;
    public enum InvitationStatus {PENDIENTE, ACEPTADO, RECHAZADO};

    public Invitation() {
    }
    
    public Invitation(User invitedUser, Session session, Role role, InvitationStatus status) {
        this.invitedUser = invitedUser;
        this.session = session;
        this.role = role;
        this.status = status;
    }
}
