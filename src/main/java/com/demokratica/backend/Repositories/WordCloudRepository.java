package com.demokratica.backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demokratica.backend.Model.WordCloud;

@Repository
public interface WordCloudRepository extends JpaRepository<WordCloud, Long> {
    
}
