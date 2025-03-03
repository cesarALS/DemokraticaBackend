package com.demokratica.backend.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.demokratica.backend.Model.UserWord;

public interface UserWordRepository extends JpaRepository<UserWord, Long> {
    
    @Query("SELECT uw FROM UserWord uw WHERE uw.wordCloud.id = :wordCloudId AND uw.user.email = :userEmail")
    Optional<UserWord> findByWordCloudAndUser(@Param("wordCloudId") Long wordCloudId, @Param("userEmail") String userEmail);
}
