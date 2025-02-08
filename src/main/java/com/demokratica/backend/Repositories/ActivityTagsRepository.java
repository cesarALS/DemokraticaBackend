package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demokratica.backend.Model.ActivityTag;

public interface ActivityTagsRepository extends JpaRepository<ActivityTag, Long> {
    
}
