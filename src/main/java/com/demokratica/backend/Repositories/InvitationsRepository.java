package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demokratica.backend.Model.Invitation;

public interface InvitationsRepository extends JpaRepository<Invitation, Long> {
    
}
