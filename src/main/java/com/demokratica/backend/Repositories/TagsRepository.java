package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demokratica.backend.Model.Tag;

@Repository
public interface TagsRepository extends JpaRepository<Tag, Long> {
    
}
