package com.demokratica.backend.Services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.demokratica.backend.DTOs.SavedTextDTO;
import com.demokratica.backend.DTOs.TextCreationDTO;
import com.demokratica.backend.Exceptions.SessionNotFoundException;
import com.demokratica.backend.Exceptions.TextNotFoundException;
import com.demokratica.backend.Model.Session;
import com.demokratica.backend.Model.Text;
import com.demokratica.backend.Model.TextTag;
import com.demokratica.backend.Repositories.SessionsRepository;
import com.demokratica.backend.Repositories.TextRepository;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;

@Service
public class TextService {
    
    private TextRepository textRepository;
    private SessionsRepository sessionsRepository;

    public TextService (TextRepository textRepository, SessionsRepository sessionsRepository) {
        this.textRepository = textRepository;
        this.sessionsRepository = sessionsRepository;
    }

    public SavedTextDTO createText(Long sessionId, TextCreationDTO textCreationDTO) {
        Text text = new Text();
        text.setContent(textCreationDTO.getContent());
        text.setCreationTime(LocalDateTime.now());
        
        Session session = sessionsRepository.findById(sessionId).orElseThrow(() -> 
            new SessionNotFoundException(sessionId));
        text.setSession(session);

        text.setTags(textCreationDTO.getTags().stream().map(tagDTO -> {
            TextTag tag = new TextTag();
            tag.setTagText(tagDTO.text());
            return tag;
        }).collect(Collectors.toCollection(ArrayList::new)));

        Text saved = textRepository.save(text);

        ArrayList<TagDTO> tags = saved.getTags().stream().map(textTag -> {
            return new TagDTO(textTag.getTagText());
        }).collect(Collectors.toCollection(ArrayList::new));

        return new SavedTextDTO(saved.getId(), saved.getContent(), tags, saved.getCreationTime());
    }

    public void deleteText(Long textId) {
        //Para asegurarse de que exista
        Text text = textRepository.findById(textId).orElseThrow(() -> 
            new TextNotFoundException(textId));

        textRepository.deleteById(text.getId());
    }

    public ArrayList<SavedTextDTO> getSessionTexts(Long sessionId) {
        Session session = sessionsRepository.findById(sessionId).orElseThrow(() ->
            new SessionNotFoundException(sessionId));

        ArrayList<SavedTextDTO> savedTexts = session.getTexts().stream().map(text -> {
            Long textId = text.getId();
            String content = text.getContent();
            LocalDateTime creationTime = text.getCreationTime();

            ArrayList<TagDTO> tagDTOs = text.getTags().stream().map(tag -> {
                return new TagDTO(tag.getTagText());
            }).collect(Collectors.toCollection(ArrayList::new));
            
            return new SavedTextDTO(textId, content, tagDTOs, creationTime);
        }).collect(Collectors.toCollection(ArrayList::new));

        return savedTexts;
    }
}
