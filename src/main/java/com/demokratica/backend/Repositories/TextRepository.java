package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demokratica.backend.Model.Text;

public interface TextRepository extends JpaRepository<Text, Long> {
    
}
