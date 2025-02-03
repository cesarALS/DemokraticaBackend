package com.demokratica.backend.RestControllers;

import org.springframework.web.bind.annotation.RestController;

import com.demokratica.backend.Services.SessionService;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
public class SessionController {
    
    private SessionService sessionService;
    public SessionController (SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<?> postMethodName(@RequestBody NewSessionDTO newSessionDTO) {
        //Esta primera aproximación no incluye ni tags ni listas de invitados, porque no sé cómo hacer la deserealización de
        //listas todavía
        String title = newSessionDTO.title();
        String description = newSessionDTO.description();
        LocalDateTime startTime = newSessionDTO.startTime();
        LocalDateTime endTime = newSessionDTO.endTime();
        try {
            sessionService.createSession(title, description, startTime, endTime);
        } catch (Exception e) {
            throw e;
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    
    public record NewSessionDTO (String title, String description, LocalDateTime startTime, LocalDateTime endTime) {

    }
}
