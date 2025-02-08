package com.demokratica.backend.Services;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Session;
import com.demokratica.backend.Model.SessionTag;
import com.demokratica.backend.Model.User;
import com.demokratica.backend.Model.Invitation.InvitationStatus;
import com.demokratica.backend.Repositories.SessionsRepository;
import com.demokratica.backend.Repositories.UsersRepository;
import com.demokratica.backend.RestControllers.SessionController.NewSessionDTO;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;

import io.jsonwebtoken.lang.Collections;
import jakarta.transaction.Transactional;

@Service
public class SessionService {
    
    private SessionsRepository sessionsRepository;
    private UsersRepository usersRepository;
    public SessionService (SessionsRepository sessionsRepository, UsersRepository usersRepository) {
        this.sessionsRepository = sessionsRepository;
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

        ArrayList<Invitation> invitedUsers = newSessionDTO.invitations().stream().map(dto -> {
            String userEmail = dto.invitedUserEmail();
            User user = usersRepository.findById(userEmail).orElseThrow(() -> new RuntimeException("Couldn't find user with email " + userEmail));

            return new Invitation(user, newSession, dto.role(), InvitationStatus.PENDIENTE);
        }).collect(Collectors.toCollection(ArrayList::new));

        //También debemos añadir al usuario que creó la sesión con rol de DUEÑO y status de invitación ACEPTADO
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //TODO: esta operacion puede causar problemas a futuro porque hay tres metodos de autenticacion que manejamos: JWT, usuario contraseña y en el futuro OAuth
        //Es posible que no sirva bien con todos
        String ownerEmail = (String) auth.getPrincipal();
        User owner = usersRepository.findById(ownerEmail).orElseThrow(() -> new RuntimeException("Couldn't find user with email " + ownerEmail));
        invitedUsers.add(new Invitation(owner, newSession, Invitation.Role.DUEÑO, InvitationStatus.ACEPTADO));

        newSession.setInvitedUsers(invitedUsers);


        List<SessionTag> tags = newSessionDTO.tags().stream().map(dto -> {
            SessionTag tag = new SessionTag();
            tag.setTagText(dto.text());
            tag.setSession(newSession);
            return tag;
        }).toList();
        newSession.setTags(tags);
        
        sessionsRepository.save(newSession);
    }

    public List<GetSessionsDTO> getSessionsOfUser(String userEmail) {
        User user = usersRepository.findById(userEmail).orElseThrow(() -> new RuntimeException("Couldn't find user with email " + userEmail));
        List<Invitation> invitations = user.getInvitations();
        List<GetSessionsDTO> sessions = invitations.stream().map(invitation -> {
            Session session = invitation.getSession();
            String title = session.getTitle();
            String description = session.getDescription();
            int noParticipants = session.getInvitedUsers().size();
            int noActivities = session.getActivities().size();
            boolean isHost = (invitation.getRole() == Invitation.Role.DUEÑO) ? true : false;

            List<TagDTO> tags = session.getTags().stream().map(tag -> {
                return new TagDTO(tag.getTagText());
            }).toList();

            String creationDate = session.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            return new GetSessionsDTO(title, description, creationDate, noParticipants, noActivities, isHost, tags);
        }).toList();

        return sessions;
    }

    public record GetSessionsDTO (String title, String description, String creationDate, int noParticipants, int noActivities, boolean isHost, List<TagDTO> tags) {
    }
}
