package com.demokratica.backend.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Invitation.Role;


public interface InvitationsRepository extends JpaRepository<Invitation, Long> {
    
    // Method to get the role based on User and session_id
    @Query("SELECT i.role FROM Invitation i WHERE i.invitedUser.email = :userEmail AND i.session.id = :sessionId")
    Optional<Role> findRoleByUserAndSessionId(@Param("userEmail") String userEmail, @Param("sessionId") Long sessionId);
}
