package com.demokratica.backend.RestControllers;

import org.springframework.web.bind.annotation.RestController;

import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Services.SessionService;
import com.demokratica.backend.Services.SessionService.GetSessionsDTO;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
public class SessionController {
    
    private SessionService sessionService;
    public SessionController (SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<?> createNewSession(@RequestBody NewSessionDTO newSessionDTO) {
        sessionService.createSession(newSessionDTO);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> returnAllUserSessions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (String) auth.getPrincipal();
        List<GetSessionsDTO> getSessionsResponse = sessionService.getSessionsOfUser(userEmail);
        return new ResponseEntity<>(getSessionsResponse, HttpStatus.OK);
    }
    
    
    public record NewSessionDTO (String title, String description, LocalDateTime startTime, LocalDateTime endTime, List<InvitationDTO> invitations, List<TagDTO> tags) {
    }

    public record InvitationDTO (Invitation.Role role, String invitedUserEmail) {
    }
    
    public record TagDTO(String text) {
    }
}
