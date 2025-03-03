package com.demokratica.backend.Repositories;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.demokratica.backend.Model.WordCloud;

@Repository
public interface WordCloudRepository extends JpaRepository<WordCloud, Long> {
    
    ArrayList<WordCloud> findBySessionId(@Param("sessionId") Long sessionId);
}
