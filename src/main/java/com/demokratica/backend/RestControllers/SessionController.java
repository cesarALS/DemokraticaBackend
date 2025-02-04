package com.demokratica.backend.RestControllers;

import org.springframework.web.bind.annotation.RestController;

import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Services.SessionService;

import java.time.LocalDateTime;
import java.util.List;

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
        sessionService.createSession(newSessionDTO);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    
    public record NewSessionDTO (String title, String description, LocalDateTime startTime, LocalDateTime endTime, List<InvitationDTO> invitations, List<TagDTO> tags) {
    }

    public record InvitationDTO (Invitation.Role role, String invitedUserEmail) {
    }
    
    public record TagDTO(String text) {
    }
}
