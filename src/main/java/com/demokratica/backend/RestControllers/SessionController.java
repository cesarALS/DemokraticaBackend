package com.demokratica.backend.RestControllers;

import org.springframework.web.bind.annotation.RestController;

import com.demokratica.backend.Exceptions.UnsupportedAuthenticationException;
import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Security.SecurityConfig;
import com.demokratica.backend.Services.SessionService;
import com.demokratica.backend.Services.SessionService.GetSessionsDTO;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController
public class SessionController {
    
    private SessionService sessionService;
    public SessionController (SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> returnAllUserSessions() {
        try {
            String userEmail = SecurityConfig.getUsernameFromAuthentication();
            List<GetSessionsDTO> getSessionsResponse = sessionService.getSessionsOfUser(userEmail);
            return new ResponseEntity<>(getSessionsResponse, HttpStatus.OK);
        } catch (UnsupportedAuthenticationException e) {
            return e.getResponse();
        }
    }

    @PostMapping("/sessions")
    public ResponseEntity<?> createNewSession(@RequestBody NewSessionDTO newSessionDTO) {
        sessionService.createSession(newSessionDTO);

        return new ResponseEntity<>(HttpStatus.CREATED);
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
    
    public record NewSessionDTO (String title, String description, LocalDateTime startTime, LocalDateTime endTime, List<InvitationDTO> invitations, List<TagDTO> tags) {
    }

    public record InvitationDTO (Invitation.Role role, String invitedUserEmail) {
    }
    
    public record TagDTO(String text) {
    }
}
