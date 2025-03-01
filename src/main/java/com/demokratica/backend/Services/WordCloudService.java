package com.demokratica.backend.Services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.demokratica.backend.DTOs.ActivityDTO;
import com.demokratica.backend.DTOs.CreatedWordCloudDTO;
import com.demokratica.backend.Exceptions.SessionNotFoundException;
import com.demokratica.backend.Model.Session;
import com.demokratica.backend.Model.WordCloud;
import com.demokratica.backend.Model.WordCloudTag;
import com.demokratica.backend.Repositories.SessionsRepository;
import com.demokratica.backend.Repositories.WordCloudRepository;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;

@Service
public class WordCloudService {
    
    private WordCloudRepository wordCloudRepository;
    private SessionsRepository sessionsRepository;
    public WordCloudService(WordCloudRepository wordCloudRepository, SessionsRepository sessionsRepository) {
        this.wordCloudRepository = wordCloudRepository;
        this.sessionsRepository = sessionsRepository;
    }

    public CreatedWordCloudDTO createWordCloud(Long sessionId, ActivityDTO dto) {
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
        return new CreatedWordCloudDTO(saved.getId(), saved.getQuestion(), saved.getStartTime(), saved.getEndTime(), tagDTOs);
    }
}
