package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demokratica.backend.Model.Activity;

@Repository
public interface ActivitiesRepository extends JpaRepository<Activity, Long> {
    
}
