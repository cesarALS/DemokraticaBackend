package com.demokratica.backend.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demokratica.backend.Model.Poll;
import com.demokratica.backend.Model.User;
import com.demokratica.backend.Model.UserVote;

public interface UserVoteRepository extends JpaRepository<UserVote, Long> {
    
    List<UserVote> findByUserAndPoll(User user, Poll poll);
}
