package com.demokratica.backend.Services;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Session;
import com.demokratica.backend.Model.SessionTag;
import com.demokratica.backend.Model.User;
import com.demokratica.backend.Model.Invitation.InvitationStatus;
import com.demokratica.backend.Model.Invitation.Role;
import com.demokratica.backend.Repositories.InvitationsRepository;
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
    private InvitationsRepository invitationsRepository;
    public SessionService (SessionsRepository sessionsRepository, UsersRepository usersRepository, InvitationsRepository invitationsRepository) {
        this.sessionsRepository = sessionsRepository;
        this.usersRepository = usersRepository;
        this.invitationsRepository = invitationsRepository;
    }

    @Transactional
    public void createSession(NewSessionDTO newSessionDTO) {
        Session newSession = new Session();

        newSession.setTitle(newSessionDTO.title());
        newSession.setDescription(newSessionDTO.description());
        newSession.setStartTime(newSessionDTO.startTime());
        newSession.setEndTime(newSessionDTO.endTime());

        newSession.setPolls(Collections.emptyList());

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
            int noActivities = session.getPolls().size();
            boolean isHost = (invitation.getRole() == Invitation.Role.DUEÑO) ? true : false;

            List<TagDTO> tags = session.getTags().stream().map(tag -> {
                return new TagDTO(tag.getTagText());
            }).toList();

            String creationDate = session.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Long sessionId = session.getId();

            return new GetSessionsDTO(sessionId, title, description, creationDate, noParticipants, noActivities, isHost, tags);
        }).toList();

        return sessions;
    }

    public void deleteById(String userEmail, Long sessionId) {
        //Primero hay que asegurarse de que el usuario no esté borrando una sesión de la que no es dueño.
        //Si falla esto retornaremos una excepción para indicarlo, en lugar de otros métodos posibles como booleanos, Optionals, etc.
        Optional<Role> userRole = invitationsRepository.findRoleByUserAndSessionId(userEmail, sessionId);
        if (userRole.isPresent() && userRole.get() == Role.DUEÑO) {
            sessionsRepository.deleteById(sessionId);
        } else {
            //Es posible tanto que el usuario tratara de borrar una sesión de la que no es dueño como una que no existe
            //Devolvemos lo mismo en ambos casos para que no se puede inferir qué sesiones existen y cuáles no.
            throw new RuntimeException("The user with email " + userEmail + " either wasn't invited to this session, he isn't the owner or the session doesn't exist");
        }
    }

    public record GetSessionsDTO (Long id, String title, String description, String creationDate, int noParticipants, int noActivities, boolean isHost, List<TagDTO> tags) {
    }
}
