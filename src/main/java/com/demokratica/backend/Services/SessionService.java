package com.demokratica.backend.Services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.demokratica.backend.Model.Session;
import com.demokratica.backend.Repositories.SessionsRepository;

import io.jsonwebtoken.lang.Collections;
import jakarta.transaction.Transactional;

@Service
public class SessionService {
    
    private SessionsRepository sessionsRepository;
    public SessionService (SessionsRepository sessionsRepository) {
        this.sessionsRepository = sessionsRepository;
    }

    @Transactional
    public void createSession(String title, String description, LocalDateTime startTime, LocalDateTime endTime) {
        Session newSession = new Session();
        newSession.setTitle(title);
        newSession.setDescription(description);
        newSession.setStartTime(startTime);
        newSession.setEndTime(endTime);

        //No tiene tags y el único invitado es el usuario que lo creó. Lo de no tener actividades sí está como debería
        newSession.setActivities(Collections.emptyList());
        newSession.setTags(Collections.emptyList());
        //TODO: hacer que "invite" al usuario que la creó y de una vez lo ponga como que ya la aceptó
        //Recordar que el usuario que lo creó debe tener rol de dueño.
        //Para hacer esto muy seguramente deba modificar el JwtAuthentication para poder extraer de él el correo, o hacer algo similar
        newSession.setInvitedUsers(Collections.emptyList());
        
        sessionsRepository.save(newSession);
    }

}
