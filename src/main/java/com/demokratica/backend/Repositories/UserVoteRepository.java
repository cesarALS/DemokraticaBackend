package com.demokratica.backend.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.demokratica.backend.Model.Poll;
import com.demokratica.backend.Model.PollOption;
import com.demokratica.backend.Model.User;
import com.demokratica.backend.Model.UserVote;

public interface UserVoteRepository extends JpaRepository<UserVote, Long> {
    
    List<UserVote> findByUserAndPoll(User user, Poll poll);
}
