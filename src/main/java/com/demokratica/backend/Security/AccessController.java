package com.demokratica.backend.Security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.demokratica.backend.Exceptions.AccessDeniedException;
import com.demokratica.backend.Exceptions.TextNotFoundException;
import com.demokratica.backend.Exceptions.WordCloudNotFoundException;
import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Text;
import com.demokratica.backend.Model.WordCloud;
import com.demokratica.backend.Repositories.InvitationsRepository;
import com.demokratica.backend.Repositories.TextRepository;
import com.demokratica.backend.Repositories.WordCloudRepository;

@Component
public class AccessController {
    
    @Autowired
    private InvitationsRepository invitationsRepository;
    @Autowired
    private WordCloudRepository wordCloudRepository;
    @Autowired
    private TextRepository textRepository;


    private Invitation.Role checkIfCanDoActionInSession(Long sessionId, Invitation.Role neededRole, RuntimeException ex) {
        String userEmail = SecurityConfig.getUsernameFromAuthentication();
        Optional<Invitation.Role> role = invitationsRepository.findRoleByUserAndSessionId(userEmail, sessionId);
        //Los roles están organizados en DUEÑO, ADMIN, EDITOR y PARTICIPANTE. Un rol numéricamente mayor al requerido 
        //significa que no se tienen los permisos suficientes
        if (!role.isPresent() || role.get().ordinal() > neededRole.ordinal()) {
            throw ex;
        }

        return role.get();
    }
    
    public void checkifCanUpdateSession(Long sessionId) {
        RuntimeException ex = new AccessDeniedException(AccessDeniedException.Type.UPDATE_SESSION);
        checkIfCanDoActionInSession(sessionId, Invitation.Role.DUEÑO, ex);
    }

    public void checkIfCanCreateActivity(Long sessionId) {
        RuntimeException ex = new AccessDeniedException(AccessDeniedException.Type.CREATE_ACTIVITY);
        checkIfCanDoActionInSession(sessionId, Invitation.Role.DUEÑO, ex);
    }

    public void checkIfCanDeleteActivities(Long sessionId) {
        RuntimeException ex = new AccessDeniedException(AccessDeniedException.Type.DELETE_ACTIVITY);
        checkIfCanDoActionInSession(sessionId, Invitation.Role.DUEÑO, ex);
    }

    public Invitation.Role checkIfCanParticipate(Long sessionId) {
        RuntimeException ex = new AccessDeniedException(AccessDeniedException.Type.PARTICIPATE_IN_ACTIVITY);
        return checkIfCanDoActionInSession(sessionId, Invitation.Role.PARTICIPANTE, ex);
    }

    public void checkIfCanParticipateInWordCloud(Long wordCloudId) {
        Optional<WordCloud> wc = wordCloudRepository.findById(wordCloudId);
        if (!wc.isPresent()) {
            throw new WordCloudNotFoundException(wordCloudId);
        }
        Long sessionId = wc.get().getSession().getId();
        checkIfCanParticipate(sessionId);
    }

    public void checkIfCanDeleteWordClouds(Long wordCloudId) {
        WordCloud wc = wordCloudRepository.findById(wordCloudId).orElseThrow(() -> 
            new WordCloudNotFoundException(wordCloudId));

        RuntimeException ex = new AccessDeniedException(AccessDeniedException.Type.DELETE_ACTIVITY);
        checkIfCanDoActionInSession(wc.getSession().getId(), Invitation.Role.DUEÑO, ex);
    }

    public void checkIfCanDeleteTexts(Long textId) {
        Text text = textRepository.findById(textId).orElseThrow(() -> 
            new TextNotFoundException(textId));

        RuntimeException ex = new AccessDeniedException(AccessDeniedException.Type.DELETE_ACTIVITY);
        checkIfCanDoActionInSession(text.getSession().getId(), Invitation.Role.DUEÑO, ex);
    }
}
