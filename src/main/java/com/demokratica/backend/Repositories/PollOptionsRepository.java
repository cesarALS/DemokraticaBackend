package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demokratica.backend.Model.PollOption;

@Repository
public interface PollOptionsRepository extends JpaRepository<PollOption, Long> {
    
}
