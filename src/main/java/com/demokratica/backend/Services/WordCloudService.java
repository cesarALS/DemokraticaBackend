package com.demokratica.backend.Services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.demokratica.backend.DTOs.ActivityCreationDTO;
import com.demokratica.backend.DTOs.WordCloudDTO;
import com.demokratica.backend.Exceptions.SessionNotFoundException;
import com.demokratica.backend.Exceptions.UserNotFoundException;
import com.demokratica.backend.Exceptions.WordCloudNotFoundException;
import com.demokratica.backend.Model.Session;
import com.demokratica.backend.Model.User;
import com.demokratica.backend.Model.UserWord;
import com.demokratica.backend.Model.WordCloud;
import com.demokratica.backend.Model.WordCloudTag;
import com.demokratica.backend.Repositories.SessionsRepository;
import com.demokratica.backend.Repositories.UserWordRepository;
import com.demokratica.backend.Repositories.UsersRepository;
import com.demokratica.backend.Repositories.WordCloudRepository;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;
import com.demokratica.backend.Security.SecurityConfig;

import jakarta.transaction.Transactional;

@Service
public class WordCloudService {
    
    private WordCloudRepository wordCloudRepository;
    private SessionsRepository sessionsRepository;
    private UsersRepository usersRepository;
    private UserWordRepository userWordRepository;
    public WordCloudService(WordCloudRepository wordCloudRepository, SessionsRepository sessionsRepository,
                            UsersRepository usersRepository, UserWordRepository userWordRepository) {
        
        this.wordCloudRepository = wordCloudRepository;
        this.sessionsRepository = sessionsRepository;
        this.usersRepository = usersRepository;
        this.userWordRepository = userWordRepository;
    }

    @Transactional
    public WordCloudDTO createWordCloud(Long sessionId, ActivityCreationDTO dto) {
        Session session = sessionsRepository.findById(sessionId).orElseThrow(() -> 
                new SessionNotFoundException(sessionId));

        WordCloud wordCloud = new WordCloud();
        wordCloud.setQuestion(dto.getQuestion());
        wordCloud.setCreationTime(LocalDateTime.now());
        wordCloud.setStartTime(dto.getStartTime());
        wordCloud.setEndTime(dto.getEndTime());
        wordCloud.setTags(dto.getTags().stream().map(tagDTO -> {
            WordCloudTag tag = new WordCloudTag();
            tag.setTagText(tagDTO.text());
            return tag;
        }).toList());

        wordCloud.setSession(session);
        wordCloud.setWords(Collections.emptyList());

        WordCloud saved = wordCloudRepository.save(wordCloud);

        ArrayList<TagDTO> tagDTOs = saved.getTags().stream().map(tag -> {
            return new TagDTO(tag.getTagText());
        }).collect(Collectors.toCollection(ArrayList::new));
        
        return new WordCloudDTO(saved.getId(), saved.getQuestion(), false, saved.getStartTime(), saved.getEndTime(), saved.getCreationTime(), tagDTOs);
    }

    @Transactional
    public void postWord(Long wordCloudId, String word) {
        String userEmail = SecurityConfig.getUsernameFromAuthentication();
        Optional<UserWord> userWord = userWordRepository.findByWordCloudAndUser(wordCloudId, userEmail);
        if (!userWord.isPresent()) {
            WordCloud wordCloud = wordCloudRepository.findById(wordCloudId).orElseThrow(() ->
            new WordCloudNotFoundException(wordCloudId));

            User user = usersRepository.findById(userEmail).orElseThrow(() -> 
                new UserNotFoundException(userEmail));

            UserWord newUserWord = new UserWord();
            newUserWord.setUser(user);
            newUserWord.setWordCloud(wordCloud);
            newUserWord.setWord(word);
            userWordRepository.save(newUserWord);
        } else {
            userWord.get().setWord(word);
            userWordRepository.save(userWord.get());
        }
    }


}
