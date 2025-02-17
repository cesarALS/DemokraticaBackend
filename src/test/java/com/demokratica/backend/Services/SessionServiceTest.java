package com.demokratica.backend.Services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.demokratica.backend.Exceptions.UserNotFoundException;
import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Invitation.InvitationStatus;
import com.demokratica.backend.Model.Session;
import com.demokratica.backend.Model.SessionTag;
import com.demokratica.backend.Model.User;
import com.demokratica.backend.Repositories.InvitationsRepository;
import com.demokratica.backend.Repositories.SessionsRepository;
import com.demokratica.backend.Repositories.UsersRepository;
import com.demokratica.backend.RestControllers.SessionController.InvitationDTO;
import com.demokratica.backend.RestControllers.SessionController.NewSessionDTO;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {
    
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private InvitationsRepository invitationsRepository;
    @Mock
    private SessionsRepository sessionsRepository;

    @InjectMocks
    private SessionService sessionService;
    
    /*
     *  Para mayor comodidad es mejor usar los mismos atributos para todas las sesiones que se van
     *  a probar y solo asegurarse de que se borren de la BD después de realizar cada prueba
     */
    private String sessionTitle = "Sesión de prueba";
    private String sessionDescription = "Probando el servicio de sesiones";
    private LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime endTime = startTime.plusHours(1);
    private ArrayList<TagDTO> tags = new ArrayList<>(List.of(new TagDTO("Prueba unitaria"), new TagDTO("Servicio de sesiones")));
    private String ownerEmail = "owner@gmail.com";
    private String invitedUserEmail = "invitedUser@gmail.com";

    @BeforeEach
    public void setup() {
        User owner = new User();
        owner.setEmail(ownerEmail);

        User invitedUser = new User();
        invitedUser.setEmail(invitedUserEmail);

        when(usersRepository.findById(ownerEmail)).thenReturn(Optional.of(owner));
        when(usersRepository.findById(invitedUserEmail)).thenReturn(Optional.of(invitedUser));
    }

    @Test
    @Order(1)
    @DisplayName("Prueba 1: invitar varias veces al mismo usuario")
    @Rollback(value = true)
    public void inviteSameUserTwice() {
        ArrayList<InvitationDTO> invitationDTOs = new ArrayList<>();
        invitationDTOs.add(new InvitationDTO(Invitation.Role.PARTICIPANTE, invitedUserEmail));
        invitationDTOs.add(new InvitationDTO(Invitation.Role.PARTICIPANTE, invitedUserEmail));

        NewSessionDTO dto = this.createMockSession(invitationDTOs);
        this.validateSavedInvitations(2, dto);
    }

    @Test
    @Order(2)
    @DisplayName("Prueba 2: invitar al mismo usuario con roles distintos")
    @Rollback(value = true)
    public void inviteSameUserDifferentRoles() {
        ArrayList<InvitationDTO> invitationDTOs = new ArrayList<>();
        invitationDTOs.add(new InvitationDTO(Invitation.Role.PARTICIPANTE, invitedUserEmail));
        invitationDTOs.add(new InvitationDTO(Invitation.Role.EDITOR, invitedUserEmail));
        invitationDTOs.add(new InvitationDTO(Invitation.Role.ADMIN, invitedUserEmail));

        this.validateSavedInvitations(2, this.createMockSession(invitationDTOs));
    }

    @Test
    @Order(3)
    @DisplayName("Prueba 3: invitar al dueño")
    @Rollback(value = true)
    public void inviteOwner() {
        ArrayList<InvitationDTO> invitationDTOs = new ArrayList<>();
        invitationDTOs.add(new InvitationDTO(Invitation.Role.PARTICIPANTE, ownerEmail));

        this.validateSavedInvitations(1, this.createMockSession(invitationDTOs));
    }

    @Test
    @Order(4)
    @DisplayName("Prueba 4: invitar a alguien con rol de dueño")
    @Rollback(value = true)
    public void inviteWithOwnerRole() {
        ArrayList<InvitationDTO> invitationDTOs = new ArrayList<>();
        invitationDTOs.add(new InvitationDTO(Invitation.Role.DUEÑO, invitedUserEmail));

        //TODO: debería lanzar algún tipo de excepción directamente
        this.validateSavedInvitations(1, this.createMockSession(invitationDTOs));
    }

    public NewSessionDTO createMockSession(ArrayList<InvitationDTO> invitationDTOs) {
        NewSessionDTO newSessionDTO = new NewSessionDTO(sessionTitle, sessionDescription, startTime, endTime, invitationDTOs, tags);
        Session mockSession = new Session();
        mockSession.setTitle(newSessionDTO.title());
        mockSession.setDescription(newSessionDTO.description());
        mockSession.setStartTime(newSessionDTO.startTime());
        mockSession.setEndTime(newSessionDTO.endTime());

        mockSession.setPolls(Collections.emptyList());
        ArrayList<SessionTag> tags = newSessionDTO.tags().stream().map(dto -> {
            SessionTag tag = new SessionTag();
            tag.setTagText(dto.text());
            tag.setSession(mockSession);
            return tag;
        }).collect(Collectors.toCollection(ArrayList::new));
        mockSession.setTags(tags);

        ArrayList<Invitation> invitedUsers = newSessionDTO.invitations().stream().map(dto -> {
            String userEmail = dto.invitedUserEmail();
            User user = usersRepository.findById(userEmail).orElseThrow(() -> 
                    new UserNotFoundException(userEmail));

            return new Invitation(user, mockSession, dto.role(), InvitationStatus.PENDIENTE);
        }).collect(Collectors.toCollection(ArrayList::new));
        //También debemos añadir al usuario que creó la sesión con rol de DUEÑO y status de invitación ACEPTADO
        User owner = usersRepository.findById(ownerEmail).orElseThrow(() -> 
                    new UserNotFoundException(ownerEmail));
        invitedUsers.add(new Invitation(owner, mockSession, Invitation.Role.DUEÑO, InvitationStatus.ACEPTADO));
        mockSession.setInvitations(invitedUsers);

        mockSession.setId(1L);

        when(sessionsRepository.save(any(Session.class))).thenReturn(mockSession);
        when(sessionsRepository.findById(1L)).thenReturn(Optional.of(mockSession));

        return newSessionDTO;
    }

    public void validateSavedInvitations(int expectedValue, NewSessionDTO dto) {
        sessionService.createSession(ownerEmail, dto);

        Optional<Session> session = sessionsRepository.findById(1L);
        Assertions.assertThat(session.isPresent());
        ArrayList<Invitation> savedInvitations = new ArrayList<>(session.get().getInvitations());
        Assertions.assertThat(savedInvitations.size()).isEqualTo(expectedValue);
    }
}
