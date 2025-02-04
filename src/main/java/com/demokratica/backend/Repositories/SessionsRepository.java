package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demokratica.backend.Model.Session;

@Repository
public interface SessionsRepository extends JpaRepository<Session, Long> {
    
}
