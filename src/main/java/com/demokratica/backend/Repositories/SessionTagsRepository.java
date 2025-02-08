package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demokratica.backend.Model.SessionTag;

@Repository
public interface SessionTagsRepository extends JpaRepository<SessionTag, Long> {
    
}
