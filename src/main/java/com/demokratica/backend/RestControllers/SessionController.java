package com.demokratica.backend.RestControllers;

import org.springframework.web.bind.annotation.RestController;

import com.demokratica.backend.Exceptions.InvalidInvitationsException;
import com.demokratica.backend.Exceptions.UnsupportedAuthenticationException;
import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Session;
import com.demokratica.backend.Security.SecurityConfig;
import com.demokratica.backend.Services.SessionService;
import com.demokratica.backend.Services.SessionService.GetSessionsDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;





@RestController
public class SessionController {
    
    private SessionService sessionService;
    public SessionController (SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/api/sessions")
    public ResponseEntity<?> returnAllUserSessions() {
        try {
            String userEmail = SecurityConfig.getUsernameFromAuthentication();
            ArrayList<GetSessionsDTO> getSessionsResponse = sessionService.getSessionsOfUser(userEmail);
            return new ResponseEntity<>(getSessionsResponse, HttpStatus.OK);
        } catch (UnsupportedAuthenticationException e) {
            return e.getResponse();
        }
    }

    @PostMapping("/api/sessions")
    public ResponseEntity<?> createNewSession(@RequestBody NewSessionDTO newSessionDTO) {
        try {
            String userEmail = SecurityConfig.getUsernameFromAuthentication();
            Session createdSession = sessionService.createSession(userEmail, newSessionDTO);
            NewSessionResponse response = new NewSessionResponse(createdSession.getId(), newSessionDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (UnsupportedAuthenticationException e) {
            return e.getResponse();
        } catch (InvalidInvitationsException e) {
            return e.getResponse();
        }
    }

    @PutMapping("api/sessions/{id}")
    public ResponseEntity<?> updateSession(@PathVariable Long id, @RequestBody NewSessionDTO dto) {
        try {
            String userEmail = SecurityConfig.getUsernameFromAuthentication();
            Session updatedSession = sessionService.updateSession(id, userEmail, dto);
            NewSessionResponse response = new NewSessionResponse(updatedSession.getId(), dto);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (UnsupportedAuthenticationException e) {
            return e.getResponse();
        }
    }
    
    //Retornar 204 (No Content) si se borró exitosamente, 403 (Forbidden) en caso de que no tenga autorización o el recurso no exista
    //No se envía un 404 (Not Found) porque esa  información podría ser usada por un atacante para averiguar las IDs de los recursos
    //existentes
    @DeleteMapping("/api/sessions/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id) {
        //Obtenemos el ID del usuario (correo) a partir de su autenticación, para así evitar que borre sesiones de otros usuarios
        //Podemos asumir que el usuario tiene una autenticación porque este endpoint está protegido por Spring Security
        try {
            String userEmail = SecurityConfig.getUsernameFromAuthentication();
            sessionService.deleteById(userEmail, id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (UnsupportedAuthenticationException e) {
            return e.getResponse();
        }
        
    }
    
    public record NewSessionDTO (String title, String description, LocalDateTime startTime, LocalDateTime endTime, ArrayList<InvitationDTO> invitations, ArrayList<TagDTO> tags) {
    }

    public record NewSessionResponse (Long id, String title, String description, LocalDateTime startTime, LocalDateTime endTime, int numParticipants, List<String> tags) {
        public NewSessionResponse(Long id, NewSessionDTO newSessionDTO) {
            this(id, newSessionDTO.title(), newSessionDTO.description(), newSessionDTO.startTime(), newSessionDTO.endTime(), 
                newSessionDTO.invitations().size(), getFormattedTags(newSessionDTO.tags()));
        }

        private static List<String> getFormattedTags(List<TagDTO> dto) {
            List<String> tags = dto.stream().map(tagDTO -> {
                return tagDTO.text();
            }).toList();

            return tags;
        }
    }

    public record InvitationDTO (Invitation.Role role, String invitedUserEmail) {
    }
    
    public record TagDTO(String text) {
    }
}
