package com.demokratica.backend.Services;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.demokratica.backend.Exceptions.UserNotFoundException;
import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Poll;
import com.demokratica.backend.Model.Session;
import com.demokratica.backend.Model.SessionTag;
import com.demokratica.backend.Model.User;
import com.demokratica.backend.Model.Invitation.InvitationStatus;
import com.demokratica.backend.Model.Invitation.Role;
import com.demokratica.backend.Repositories.InvitationsRepository;
import com.demokratica.backend.Repositories.SessionsRepository;
import com.demokratica.backend.Repositories.UsersRepository;
import com.demokratica.backend.RestControllers.SessionController.InvitationDTO;
import com.demokratica.backend.RestControllers.SessionController.NewSessionDTO;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;

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
    public void createSession(String ownerEmail, NewSessionDTO newSessionDTO) {
        Session newSession = new Session();
        ArrayList<Poll> polls = new ArrayList<>();

        ArrayList<Invitation> invitedUsers = newSessionDTO.invitations().stream().map(dto -> {
            String userEmail = dto.invitedUserEmail();
            User user = usersRepository.findById(userEmail).orElseThrow(() -> 
                    new UserNotFoundException(userEmail));

            return new Invitation(user, newSession, dto.role(), InvitationStatus.PENDIENTE);
        }).collect(Collectors.toCollection(ArrayList::new));
        //También debemos añadir al usuario que creó la sesión con rol de DUEÑO y status de invitación ACEPTADO
        User owner = usersRepository.findById(ownerEmail).orElseThrow(() -> 
                    new UserNotFoundException(ownerEmail));
        invitedUsers.add(new Invitation(owner, newSession, Invitation.Role.DUEÑO, InvitationStatus.ACEPTADO));

        sessionCreateUpdateHelper(newSession, polls, invitedUsers, newSessionDTO);
    }

    @Transactional
    public void updateSession (Long sessionId, String userEmail, NewSessionDTO updatedSessionDTO) {
        //Primero hay que verificar que el usuario tenga los permisos necesarios para reconfigurar la sesión (DUEÑO solamente, por ahora)
        Optional<Invitation.Role> role = invitationsRepository.findRoleByUserAndSessionId(userEmail, sessionId);
        role.ifPresent(presentRole -> {
            if (presentRole != Invitation.Role.DUEÑO) {
                throw new RuntimeException("User with email " + userEmail + " isn't admin of the session he is trying to update");    
            }
        });

        Session session = sessionsRepository.findById(sessionId).orElseThrow(() -> 
                                new RuntimeException("Couldn't find a session with id " + sessionId + " in the database"));
        
        ArrayList<Poll> polls = new ArrayList<>(session.getPolls());

        /*
         * El objetivo de este código es determinar qué invitaciones son nuevas y cuáles son antiguas y representan una actualización.
         * Esto es necesario porque las invitaciones tienen un status de PENDIENTE, ACEPTADO y RECHAZADO. Si un usuario ya aceptó o rechazó,
         * queremos evitar que esta información se pierda cuando el frontend nos manda una actualización de las invitaciones (como un nuevo
         * invitado). 
         * Si un usuario es un nuevo invitado, debemos poner el status de su invitación como PENDIENTE.
         * Además debemos asegurarnos de que los roles de los nuevos usuarios sean los que nos piden en el JSON y que los roles de los
         * antiguos invitados sigan siendo los mismos o sean actualizados.
         * TODO: considerar abstraerlo en una nueva función
         * TODO: buscar una manera más elegante de hacerlo. Tal vez con operaciones directamente en la BD se pueda
         */
        Set<String> oldInvitedUserEmails = session.getInvitations().stream()
                                                .map(inv -> {
                                                    return inv.getInvitedUser().getEmail();
                                                })
                                                .collect(Collectors.toSet());

        Set<String> newInvitedUserEmails = updatedSessionDTO.invitations().stream()
                                                .map(InvitationDTO::invitedUserEmail)
                                                .collect(Collectors.toSet());

        //Para los usuarios identificados con estos correos es necesario actualizar la invitación (rol) si llega a ser necesario. El
        //status de la invitación se mantiene igual
        Set<String> commonEmails = new HashSet<>(oldInvitedUserEmails);
        commonEmails.retainAll(newInvitedUserEmails);
        //Para los usuarios identificados con estos correos es necesario crear una invitación desde 0, con el rol que está en el JSON
        //y status PENDIENTE
        Set<String> newEmails = new HashSet<>(newInvitedUserEmails);
        newEmails.removeAll(oldInvitedUserEmails);

        ArrayList<Invitation> oldInvitations = new ArrayList<>();
        for (Invitation invitation : session.getInvitations()) {
            User user = invitation.getInvitedUser();
            String invitedUserEmail = user.getEmail();
            if (commonEmails.contains(invitedUserEmail)) {
                Invitation.InvitationStatus status = invitation.getStatus();
                //Valor previo a la actualización
                Invitation.Role newRole = invitation.getRole();
                for (InvitationDTO invDto : updatedSessionDTO.invitations()) {
                    if (invitedUserEmail.equals(invDto.invitedUserEmail())) {
                        newRole = invDto.role();
                        break;
                    }
                }

                oldInvitations.add(new Invitation(user, session, newRole, status));
            }
        }

        ArrayList<Invitation> newInvitations = new ArrayList<>();
        for (InvitationDTO invDto : updatedSessionDTO.invitations()) {
            String email = invDto.invitedUserEmail();
            if (newEmails.contains(email)) {
                User user = usersRepository.findById(email).orElseThrow(() -> 
                    new UserNotFoundException(email)
                );
                
                newInvitations.add(new Invitation(user, session, invDto.role(), InvitationStatus.PENDIENTE));
            }
        }

        ArrayList<Invitation> entireInvitations = new ArrayList<>();
        entireInvitations.addAll(oldInvitations);
        entireInvitations.addAll(newInvitations);

        sessionCreateUpdateHelper(session, polls, entireInvitations, updatedSessionDTO);
    }

    //Para usarlo para crear sesión, pasarle una nueva sesión
    //Para usarlo para actualizar una sesión, pasarle la sesión a actualizar
    //El usuario de esta función...
    //TODO: agregar soporte para fecha de actualización y fecha de publicación
    @Transactional
    private void sessionCreateUpdateHelper (Session session, ArrayList<Poll> polls, ArrayList<Invitation> invitedUsers, NewSessionDTO newSessionDTO) {
            session.setTitle(newSessionDTO.title());
            session.setDescription(newSessionDTO.description());
            session.setStartTime(newSessionDTO.startTime());
            session.setEndTime(newSessionDTO.endTime());
    
            session.setPolls(polls);
            session.setInvitations(invitedUsers);
            
            //NOTA: Esta lógica es independiente de si se está usando la función para crear o para actualizar
            ArrayList<SessionTag> tags = newSessionDTO.tags().stream().map(dto -> {
                SessionTag tag = new SessionTag();
                tag.setTagText(dto.text());
                tag.setSession(session);
                return tag;
            }).collect(Collectors.toCollection(ArrayList::new));
            session.setTags(tags);
            
            sessionsRepository.save(session);
    }
    
    public ArrayList<GetSessionsDTO> getSessionsOfUser(String userEmail) {
        User user = usersRepository.findById(userEmail).orElseThrow(() -> new RuntimeException("Couldn't find user with email " + userEmail));
        ArrayList<Invitation> invitations = new ArrayList<>(user.getInvitations());
        ArrayList<GetSessionsDTO> sessions = invitations.stream().map(invitation -> {
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

            return new GetSessionsDTO(sessionId, title, description, creationDate, noParticipants, noActivities, isHost, tags);
        }).collect(Collectors.toCollection(ArrayList::new));

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

    public record GetSessionsDTO (Long id, String title, String description, String creationDate, int noParticipants, int noActivities, boolean isHost, ArrayList<TagDTO> tags) {
    }
}
