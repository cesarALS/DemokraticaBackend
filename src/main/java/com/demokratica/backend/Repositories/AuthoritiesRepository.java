package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demokratica.backend.Model.Authority;
import com.demokratica.backend.Model.User;

public interface AuthoritiesRepository extends JpaRepository<Authority, Long> {
    Authority findByUser(User user);
}
