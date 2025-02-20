package com.demokratica.backend.Model;

import java.util.Objects;

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
    @Column(name = "user_role")
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != getClass()) return false;
        Invitation that = (Invitation) o;
        return (that.getInvitedUser().getEmail().equals(this.getInvitedUser().getEmail())) &&
               (that.getSession().getId() == this.getSession().getId()) &&
               (that.getRole() == this.getRole()) &&
               (that.getStatus() == this.getStatus());
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.invitedUser.getEmail(), this.session.getId(), this.role, this.status);
    }
}
