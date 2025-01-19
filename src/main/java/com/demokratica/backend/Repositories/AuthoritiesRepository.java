package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demokratica.backend.Model.Authority;

public interface AuthoritiesRepository extends JpaRepository<Authority, Long> {
    
}
