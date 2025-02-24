package com.demokratica.backend.Repositories;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Poll;
import com.demokratica.backend.Services.PollService.PollResultDTO;

@Repository
public interface PollsRepository extends JpaRepository<Poll, Long> {
    
    @Query("SELECT i.role FROM Poll p " +
           "JOIN Invitation i ON i.session = p.session " +
            "WHERE p.id = :poll_id AND i.invitedUser.email = :userEmail")
    Optional<Invitation.Role> findPollOwner(@Param("poll_id") Long poll_id, @Param("userEmail") String userEmail);

    @Query("SELECT new PollResultDTO(po.id, po.description, COUNT(uv.id)) " +
            "FROM PollOption po " + 
            "LEFT JOIN UserVote uv ON uv.option.id = po.id " + 
            "WHERE po.poll.id = :pollId " + 
            "GROUP BY po.id, po.description")
    ArrayList<PollResultDTO> getPollResults (@Param("pollId") Long pollId);

    @Query("SELECT COUNT(i) FROM Invitation i WHERE i.session.id = :sessionId")
    Long getTotalInvitedUsers (@Param("sessionId") Long sessionId);
}
