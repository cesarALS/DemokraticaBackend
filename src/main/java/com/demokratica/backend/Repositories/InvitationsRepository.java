package com.demokratica.backend.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Invitation.InvitationStatus;
import com.demokratica.backend.Model.Invitation.Role;


public interface InvitationsRepository extends JpaRepository<Invitation, Long> {
    
    @Query("SELECT i.role FROM Invitation i WHERE i.invitedUser.email = :userEmail AND i.session.id = :sessionId")
    Optional<Role> findRoleByUserAndSessionId(@Param("userEmail") String userEmail, @Param("sessionId") Long sessionId);

    @Query("SELECT i.status FROM Invitation i WHERE i.invitedUser.email = :userEmail AND i.session.id = :sessionId")
    Optional<InvitationStatus> findInvitationStatusByEmailAndSessionId(@Param("userEmail") String userEmail, @Param("sessionId") Long sessionId);

    @Query("SELECT i FROM invitation i WHERE i.invitedUser.email = :userEmail AND i.session.id = :sessionId")
    Optional<Invitation> findInvitationByUserAndSessionId(@Param("userEmail") String userEmail, @Param("sessionId") Long sessionId);

    
}
