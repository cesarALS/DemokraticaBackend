package com.demokratica.backend.Security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.demokratica.backend.Exceptions.AccessDeniedException;
import com.demokratica.backend.Exceptions.WordCloudNotFoundException;
import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.WordCloud;
import com.demokratica.backend.Repositories.InvitationsRepository;
import com.demokratica.backend.Repositories.WordCloudRepository;

@Component
public class AccessController {
    
    @Autowired
    private InvitationsRepository invitationsRepository;
    @Autowired
    private WordCloudRepository wordCloudRepository;

    public void checkifCanUpdateSession(Long sessionId) {
        String userEmail = SecurityConfig.getUsernameFromAuthentication();
        //Múltiples razones posibles para no permitirle actualizar una sesión: no tiene el rol necesario,
        //directamente no ha sido invitado a la sesión o la sesión ni siquiera existe
        Optional<Invitation.Role> role = invitationsRepository.findRoleByUserAndSessionId(userEmail, sessionId);
        if (!role.isPresent() || role.get() != Invitation.Role.DUEÑO) {
            throw new AccessDeniedException(AccessDeniedException.Type.UPDATE_SESSION);
        }
    }

    public void checkIfCanCreateActivity(Long sessionId) {
        String userEmail = SecurityConfig.getUsernameFromAuthentication();
        Optional<Invitation.Role> role = invitationsRepository.findRoleByUserAndSessionId(userEmail, sessionId);
        if (!role.isPresent() || role.get() != Invitation.Role.DUEÑO) {
            throw new AccessDeniedException(AccessDeniedException.Type.CREATE_ACTIVITY);
        }
    }

    public void checkIfCanParticipate(Long sessionId) {
        String userEmail = SecurityConfig.getUsernameFromAuthentication();
        Optional<Invitation.Role> role = invitationsRepository.findRoleByUserAndSessionId(userEmail, sessionId);
        if (!role.isPresent()) {
            throw new AccessDeniedException(AccessDeniedException.Type.PARTICIPATE_IN_ACTIVITY);
        }
    }

    public void checkIfCanParticipateInWordCloud(Long wordCloudId) {
        Optional<WordCloud> wc = wordCloudRepository.findById(wordCloudId);
        if (!wc.isPresent()) {
            throw new WordCloudNotFoundException(wordCloudId);
        }
        Long sessionId = wc.get().getSession().getId();
        checkIfCanParticipate(sessionId);
    }
}
