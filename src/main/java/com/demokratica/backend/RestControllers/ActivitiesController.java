package com.demokratica.backend.RestControllers;

import org.springframework.web.bind.annotation.RestController;

import com.demokratica.backend.Exceptions.UnsupportedAuthenticationException;
import com.demokratica.backend.Model.Poll;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;
import com.demokratica.backend.Security.SecurityConfig;
import com.demokratica.backend.Services.PollService;
import com.demokratica.backend.Services.PollService.PollDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * Esta clase va a contener todos los endpoints para crear todos los distintos tipos de actividades.
 * Por ahora está previsto que solo sea una actividad, la votación común.
 * También retorna la información de todas las actividades de una sesión.
 * Otro endpoint que contiene es el de votar por una opción en una votación común.
 */
@RestController
public class ActivitiesController {
    
    private PollService pollService;
    public ActivitiesController (PollService pollService) {
        this.pollService = pollService;
    }

    @PostMapping("/api/sessions/{id}/polls")
    public ResponseEntity<?> createPoll(@PathVariable Long id, @RequestBody NewPollDTO newPollDTO) {
        Poll createdPoll = pollService.createPoll(newPollDTO, id);
        CreatedPollResponse response = new CreatedPollResponse(createdPoll.getId(), newPollDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/api/sessions/{id}")
    public ResponseEntity<?> getActivities(@PathVariable Long id) {
        try {
            String userEmail = SecurityConfig.getUsernameFromAuthentication();
            ArrayList<PollDTO> userPolls = new ArrayList<>(pollService.getSessionPolls(id, userEmail));

            return new ResponseEntity<>(userPolls, HttpStatus.OK);
        } catch (UnsupportedAuthenticationException e) {
            return e.getResponse();
        }
    }

    @PostMapping("/api/polls/{poll_id}")
    public ResponseEntity<?> voteForAnOption(@PathVariable Long poll_id, @RequestBody VoteDTO vote) {
        try {
            String userEmail = SecurityConfig.getUsernameFromAuthentication();
            Long optionId = vote.optionId();
            pollService.voteForOption(poll_id, userEmail, optionId);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (UnsupportedAuthenticationException e) {
            return e.getResponse();
        }
    }

    @DeleteMapping("/api/polls/{poll_id}")
    public ResponseEntity<?> deletePoll (@PathVariable Long poll_id) {
        try {
            String userEmail = SecurityConfig.getUsernameFromAuthentication();
            pollService.deletePoll(userEmail, poll_id);
        } catch (UnsupportedAuthenticationException e) {
            return e.getResponse();
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    public record NewPollDTO(String title, String description, LocalDateTime startTime, LocalDateTime endTime, ArrayList<TagDTO> tags, ArrayList<PollOptionDTO> pollOptions) {
    }

    public record PollOptionDTO(Long id, String description, ArrayList<VoterDTO> voters) {
    }
    
    public record VoterDTO (String voterEmail) {
    }

    public record VoteDTO(Long optionId) {

    }

    public record PollOptionsResponse (Long id, String description) {
    }

    public record CreatedPollResponse (Long id, String title, String description, LocalDateTime startTime, 
                                        LocalDateTime endTime, List<String> tags, List<PollOptionsResponse> pollOptions) {
        
        public CreatedPollResponse (Long id, NewPollDTO newPollDTO) {
            this(id, newPollDTO.title(), newPollDTO.description(), newPollDTO.startTime(), newPollDTO.endTime(), 
                    getFormattedTags(newPollDTO.tags()), getFormattedPollOptions(newPollDTO.pollOptions()));
        }

        public static List<String> getFormattedTags(List<TagDTO> tagDTOs) {
            List<String> tags = tagDTOs.stream().map(dto -> {
                return dto.text();
            }).toList();

            return tags;
        }

        public static List<PollOptionsResponse> getFormattedPollOptions(List<PollOptionDTO> pollOptionDTOs) {
            List<PollOptionsResponse> response = pollOptionDTOs.stream().map(dto -> {
                Long id = dto.id();
                String description = dto.description();
                return new PollOptionsResponse(id, description);
            }).toList();

            return response;
        }
        
    }
    
}
