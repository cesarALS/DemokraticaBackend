package com.demokratica.backend.Services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Session;
import com.demokratica.backend.Model.Tag;
import com.demokratica.backend.Model.User;
import com.demokratica.backend.Model.Invitation.InvitationStatus;
import com.demokratica.backend.Repositories.SessionsRepository;
import com.demokratica.backend.Repositories.InvitationsRepository;
import com.demokratica.backend.Repositories.TagsRepository;
import com.demokratica.backend.Repositories.UsersRepository;
import com.demokratica.backend.RestControllers.SessionController.InvitationDTO;
import com.demokratica.backend.RestControllers.SessionController.NewSessionDTO;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;

import io.jsonwebtoken.lang.Collections;
import jakarta.transaction.Transactional;

@Service
public class SessionService {
    
    private SessionsRepository sessionsRepository;
    private InvitationsRepository invitationsRepository;
    private TagsRepository tagsRepository;
    private UsersRepository usersRepository;
    public SessionService (SessionsRepository sessionsRepository, InvitationsRepository invitationsRepository, 
                            TagsRepository tagsRepository, UsersRepository usersRepository) {
        this.sessionsRepository = sessionsRepository;
        this.invitationsRepository = invitationsRepository;
        this.tagsRepository = tagsRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional
    public void createSession(NewSessionDTO newSessionDTO) {
        Session newSession = new Session();

        newSession.setTitle(newSessionDTO.title());
        newSession.setDescription(newSessionDTO.description());
        newSession.setStartTime(newSessionDTO.startTime());
        newSession.setEndTime(newSessionDTO.endTime());

        newSession.setActivities(Collections.emptyList());

        //TODO: hacer que "invite" al usuario que la creó y de una vez lo ponga como que ya la aceptó y tiene rol de dueño
        //Para hacer esto muy seguramente deba modificar el JwtAuthentication para poder extraer de él el correo, o hacer algo similar
        List<Invitation> invitedUsers = newSessionDTO.invitations().stream().map(dto -> {
            Invitation invitation = new Invitation();
            String userEmail = dto.invitedUserEmail();
            User user = usersRepository.findById(userEmail).orElseThrow(() -> new RuntimeException("Couldn't find user with email " + userEmail));

            invitation.setInvitedUser(user);
            invitation.setRole(dto.role());
            invitation.setStatus(InvitationStatus.PENDIENTE);
            invitation.setSession(newSession);
            return invitation;
        }).toList();
        newSession.setInvitedUsers(invitedUsers);

        List<Tag> tags = newSessionDTO.tags().stream().map(dto -> {
            Tag tag = new Tag();
            tag.setTagText(dto.text());
            tag.setSession(newSession);
            return tag;
        }).toList();
        newSession.setTags(tags);
        
        sessionsRepository.save(newSession);
    }

}
