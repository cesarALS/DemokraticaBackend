package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demokratica.backend.Model.User;

@Repository
public interface UsersRepository extends JpaRepository<User, String>{
    
}
