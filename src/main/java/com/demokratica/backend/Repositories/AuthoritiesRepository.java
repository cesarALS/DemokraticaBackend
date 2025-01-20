package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demokratica.backend.Model.Authority;
import com.demokratica.backend.Model.User;

@Repository
public interface AuthoritiesRepository extends JpaRepository<Authority, Long> {
    Authority findByUser(User user);
}
