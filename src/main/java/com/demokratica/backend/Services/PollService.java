package com.demokratica.backend.Services;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demokratica.backend.Model.PollTag;
import com.demokratica.backend.Model.Poll;
import com.demokratica.backend.Model.PollOption;
import com.demokratica.backend.Model.Session;
import com.demokratica.backend.Repositories.PollsRepository;
import com.demokratica.backend.Repositories.SessionsRepository;
import com.demokratica.backend.RestControllers.ActivitiesController.NewPollDTO;

import jakarta.transaction.Transactional;

@Service
public class PollService {
    
    @Autowired
    private SessionsRepository sessionsRepository;
    @Autowired
    private PollsRepository pollsRepository;

    @Transactional
    public void createPoll(NewPollDTO dto, Long sessionId) {
        //TODO: verificar que el usuario que crea la votación en esa sesión tiene rol de dueño
        Session session = sessionsRepository.findById(sessionId).orElseThrow(() -> 
            new RuntimeException("Couldn't find session with id " + String.valueOf(sessionId)));

        Poll poll = new Poll();
        poll.setSession(session);

        String title = dto.title();
        String description = dto.description();
        
        poll.setTitle(title);
        poll.setDescription(description);
        poll.setStartTime(dto.startTime());
        poll.setEndTime(dto.endTime());
        
        poll.setTags(dto.tags().stream().map(tagDto -> {
            PollTag tag = new PollTag();
            String tagText = tagDto.text();
            tag.setTagText(tagText);
            tag.setPoll(poll);

            return tag;
        }).toList());

        poll.setOptions(dto.pollOptions().stream().map(optionDto -> {
            PollOption option = new PollOption();
            String optionDescription = optionDto.description();
            option.setDescription(optionDescription);
            option.setVotes(Collections.emptyList());
            option.setPoll(poll);

            return option;
        }).toList());
        
        pollsRepository.save(poll);
    }
}
