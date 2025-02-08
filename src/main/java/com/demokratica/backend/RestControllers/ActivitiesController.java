package com.demokratica.backend.RestControllers;

import org.springframework.web.bind.annotation.RestController;

import com.demokratica.backend.RestControllers.SessionController.TagDTO;
import com.demokratica.backend.Security.JwtAuthentication;
import com.demokratica.backend.Services.PollService;
import com.demokratica.backend.Services.PollService.PollDTO;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
        pollService.createPoll(newPollDTO, id);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    
    @GetMapping("/api/sessions/{id}")
    public ResponseEntity<?> getActivities(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = "";
        if ("JwtAuthentication".equals(auth.getClass().getName())) {
            userEmail = (String) auth.getPrincipal();
        } else if ("UsernamePasswordAuthenticationToken".equals(auth.getClass().getName())) {
            userEmail = (String) ((UserDetails) auth.getPrincipal()).getUsername();
        }

        List<PollDTO> userPolls = pollService.getSessionPolls(id, userEmail);
        return new ResponseEntity<>(userPolls, HttpStatus.OK);
    }

    @PostMapping("/api/polls/{poll_id}")
    public ResponseEntity<?> voteForAnOption(@PathVariable Long poll_id, @RequestBody VoteDTO vote) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = "";
        if (JwtAuthentication.class.getName().equals(auth.getClass().getName())) {
            userEmail = (String) auth.getPrincipal();
        } else if (UsernamePasswordAuthenticationToken.class.getName().equals(auth.getClass().getName())) {
            userEmail = (String) ((UserDetails) auth.getPrincipal()).getUsername();
        }

        Long optionId = vote.optionId();
        pollService.voteForOption(poll_id, userEmail, optionId);

        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    public record NewPollDTO(String title, String description, LocalDateTime startTime, LocalDateTime endTime, List<TagDTO> tags, List<PollOptionDTO> pollOptions) {
    }

    public record PollOptionDTO(Long id, String description, List<VoterDTO> voters) {
    }
    
    public record VoterDTO (String voterEmail) {
    }

    public record VoteDTO(Long optionId) {

    }
    
}
