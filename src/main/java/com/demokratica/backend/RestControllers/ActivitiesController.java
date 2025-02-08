package com.demokratica.backend.RestControllers;

import org.springframework.web.bind.annotation.RestController;

import com.demokratica.backend.RestControllers.SessionController.TagDTO;
import com.demokratica.backend.Services.PollService;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
    
    public record NewPollDTO(String title, String description, LocalDateTime startTime, LocalDateTime endTime, List<TagDTO> tags, List<PollOptionDTO> pollOptions) {
    }

    public record PollOptionDTO(String description) {
    }
}
