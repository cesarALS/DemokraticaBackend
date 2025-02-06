package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demokratica.backend.Model.Plan;

public interface PlansRepository extends JpaRepository<Plan, Long> {
    
}
