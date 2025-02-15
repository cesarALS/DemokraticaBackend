package com.demokratica.backend.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Poll;

@Repository
public interface PollsRepository extends JpaRepository<Poll, Long> {
    
    @Query("SELECT i.role FROM Poll p " +
           "JOIN Invitation i ON i.session = p.session " +
            "WHERE p.id = :poll_id AND i.invitedUser.email = :userEmail")
    Optional<Invitation.Role> findPollOwner(@Param("poll_id") Long poll_id, @Param("userEmail") String userEmail);
}
