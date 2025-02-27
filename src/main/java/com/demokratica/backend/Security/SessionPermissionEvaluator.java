package com.demokratica.backend.Security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.demokratica.backend.Exceptions.AccessDeniedException;
import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Repositories.InvitationsRepository;

@Component
public class SessionPermissionEvaluator {
    
    @Autowired
    private InvitationsRepository invitationsRepository;

    public void checkifCanUpdateSession(String userEmail, Long sessionId) {
        //Múltiples razones posibles para no permitirle actualizar una sesión: no tiene el rol necesario,
        //directamente no ha sido invitado a la sesión o la sesión ni siquiera existe
        Optional<Invitation.Role> role = invitationsRepository.findRoleByUserAndSessionId(userEmail, sessionId);
        if (!role.isPresent() || role.get() != Invitation.Role.DUEÑO) {
            throw new AccessDeniedException(AccessDeniedException.Type.UPDATE_SESSION);
        }
    }
}
