package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demokratica.backend.Model.UserVote;

public interface UserVoteRepository extends JpaRepository<UserVote, Long> {
    
}
