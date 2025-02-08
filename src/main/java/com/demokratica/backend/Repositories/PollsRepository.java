package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demokratica.backend.Model.Poll;

@Repository
public interface PollsRepository extends JpaRepository<Poll, Long> {
    
}
