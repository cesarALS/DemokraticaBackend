package com.demokratica.backend.Services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.demokratica.backend.Exceptions.InvalidInvitationsException;
import com.demokratica.backend.Exceptions.InvalidTagsException;
import com.demokratica.backend.Exceptions.RoleNotFoundException;
import com.demokratica.backend.Exceptions.SessionNotFoundException;
import com.demokratica.backend.Exceptions.UserNotFoundException;
import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Poll;
import com.demokratica.backend.Model.Session;
import com.demokratica.backend.Model.SessionTag;
import com.demokratica.backend.Model.User;
import com.demokratica.backend.Model.Invitation.Role;
import com.demokratica.backend.Repositories.InvitationsRepository;
import com.demokratica.backend.Repositories.SessionsRepository;
import com.demokratica.backend.Repositories.UsersRepository;
import com.demokratica.backend.RestControllers.SessionController.InvitationDTO;
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
    public Session createSession(String ownerEmail, NewSessionDTO newSessionDTO) {
        Session newSession = new Session();
        List<Poll> polls = Collections.emptyList();
        List<Invitation> invitedUsers = validateInvitationList(ownerEmail, newSessionDTO.invitations(), newSession);

        return sessionCreateUpdateHelper(newSession, polls, invitedUsers, newSessionDTO);
    }

    @Transactional
    public Session updateSession (Long sessionId, String userEmail, NewSessionDTO updatedSessionDTO) {
        Session session = sessionsRepository.findById(sessionId).orElseThrow(() -> 
                                new SessionNotFoundException(sessionId));
        
        List<Poll> polls = session.getPolls();
        List<Invitation> newInvitedUsers = validateInvitationList(userEmail, updatedSessionDTO.invitations(), session);
        
        return sessionCreateUpdateHelper(session, polls, newInvitedUsers, updatedSessionDTO); 
    }

    //TODO: agregar soporte para fecha de actualización y fecha de publicación
    @Transactional
    private Session sessionCreateUpdateHelper (Session session, List<Poll> polls, List<Invitation> invitations, NewSessionDTO newSessionDTO) {
            session.setTitle(newSessionDTO.title());
            session.setDescription(newSessionDTO.description());
            session.setStartTime(newSessionDTO.startTime());
            session.setEndTime(newSessionDTO.endTime());
    
            session.setPolls(polls);
            session.setInvitations(invitations);
            
            //NOTA: Esta lógica es independiente de si se está usando la función para crear o para actualizar
            //Queremos evitar que se agreguen dos tags idénticos (mismo texto). Para eso está esta este HashMap y esta lógica
            Map<String, SessionTag> tagsMap = new HashMap<>();
            for (TagDTO dto : newSessionDTO.tags()) {
                if (tagsMap.containsKey(dto.text())) {
                    throw new InvalidTagsException();                    
                }

                SessionTag tag = new SessionTag();
                tag.setTagText(dto.text());

                tagsMap.put(dto.text(), tag);
            }
            session.setTags(new ArrayList<>(tagsMap.values()));
            
            return sessionsRepository.save(session);
    }
    
    public ArrayList<GetBriefSessionsDTO> getSessionsOfUser(String userEmail) {
        User user = usersRepository.findById(userEmail).orElseThrow(() -> 
            new UserNotFoundException(userEmail));
        
        ArrayList<Invitation> invitations = new ArrayList<>(user.getInvitations());
        ArrayList<GetBriefSessionsDTO> sessions = invitations.stream().map(invitation -> {
            Session session = invitation.getSession();

            String title = session.getTitle();
            String description = session.getDescription();
            int noParticipants = session.getInvitations().size();
            int noActivities = session.getPolls().size();
            boolean isHost = (invitation.getRole() == Invitation.Role.DUEÑO) ? true : false;

            ArrayList<TagDTO> tags = session.getTags().stream().map(tag -> {
                return new TagDTO(tag.getTagText());
            }).collect(Collectors.toCollection(ArrayList::new));

            String creationDate = session.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Long sessionId = session.getId();

            return new GetBriefSessionsDTO(sessionId, title, description, creationDate, noParticipants, noActivities, isHost, tags);
        }).collect(Collectors.toCollection(ArrayList::new));

        return sessions;
    }

    public GetDetailedSessionDTO getSessionDetails(Long sessionId) {
        Session session = sessionsRepository.findById(sessionId).orElseThrow(() ->
            new SessionNotFoundException(sessionId));

        String title = session.getTitle();
        String description = session.getDescription();
        LocalDateTime startTime = session.getStartTime();
        LocalDateTime endTime = session.getEndTime();

        ArrayList<ParticipantDTO> participants = session.getInvitations().stream().map(invitation -> {
            String email = invitation.getInvitedUser().getEmail();
            String username = invitation.getInvitedUser().getUsername();
            Invitation.Role role = invitation.getRole();

            return new ParticipantDTO(email, username, role);
        }).collect(Collectors.toCollection(ArrayList::new));

        ArrayList<TagDTO> tagsDTOs = session.getTags().stream().map(tag -> {
            return new TagDTO(tag.getTagText());
        }).collect(Collectors.toCollection(ArrayList::new));

        return new GetDetailedSessionDTO(title, description, startTime, endTime, tagsDTOs, participants);
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



    public List<Invitation> validateInvitationList(String ownerEmail, List<InvitationDTO> invitationDTOs, Session session) {
        Set<String> invitedEmails = new HashSet<>();
        Map<String, Invitation.Role> emailRoles = new HashMap<>();

        ArrayList<Invitation> invitations = new ArrayList<>();
        for (InvitationDTO invitation : invitationDTOs) {
            String invitedEmail = invitation.invitedUserEmail();
            Invitation.Role role = invitation.role();

            if (invitedEmail.equals(ownerEmail)) {
                throw new InvalidInvitationsException(InvalidInvitationsException.Type.INVITED_OWNER);
            }

            emailRoles.putIfAbsent(invitedEmail, role);
            if (!invitedEmails.add(invitedEmail)) {
                if (emailRoles.get(invitedEmail) != role) {
                    throw new InvalidInvitationsException(InvalidInvitationsException.Type.INVITED_TWICE_DIFF_ROLE);
                } else {
                    throw new InvalidInvitationsException(InvalidInvitationsException.Type.INVITED_TWICE);
                }
            }

            if (role.equals(Invitation.Role.DUEÑO) && !invitedEmail.equals(ownerEmail)) {
                throw new InvalidInvitationsException(InvalidInvitationsException.Type.INVITED_ADDITIONAL_OWNER);
            }

            User invitedUser = usersRepository.findById(invitedEmail).orElseThrow(() ->
                new UserNotFoundException(invitedEmail));
            invitations.add(new Invitation(invitedUser, session, role));
        }

        User owner = usersRepository.findById(ownerEmail).orElseThrow(() -> 
                new UserNotFoundException(ownerEmail));
        invitations.add(new Invitation(owner, session, Invitation.Role.DUEÑO));

        return invitations;
    }

    public Invitation.Role getUserRoleFromEmail(String email, long sessionId) {
        Invitation.Role userRole = invitationsRepository.findRoleByUserAndSessionId(email, sessionId)
                    .orElseThrow(() -> new RoleNotFoundException(email, sessionId));

        return userRole;
    }

    public record GetBriefSessionsDTO (Long id, String title, String description, String creationDate, int noParticipants, 
                                    int noActivities, boolean isHost, ArrayList<TagDTO> tags) {
    }

    public record GetDetailedSessionDTO (String title, String description, LocalDateTime startTime, LocalDateTime endTime,
                                        ArrayList<TagDTO> tags, ArrayList<ParticipantDTO> participants) {
    }

    public record ParticipantDTO (String email, String username, Invitation.Role role) {
    }
}
