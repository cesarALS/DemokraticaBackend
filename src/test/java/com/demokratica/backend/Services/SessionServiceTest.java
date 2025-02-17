package com.demokratica.backend.Services;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Session;
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
    private String sessionDescription = "Probando que no deje invitar al mismo más de una vez";
    private LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime endTime = startTime.plusHours(1);
    private ArrayList<TagDTO> tags = new ArrayList<>(List.of(new TagDTO("Tag de prueba 1"), new TagDTO("tag de prueba 2")));
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
        String invitedUserEmail = "invitedUser@gmail.com";

        //Debemos crear el NewSessionDTO que nos exige el servicio, lo cual es una mamera pero toca
        //Afortunadamente la mayoría de esto ya está hecho en el constructor
        ArrayList<InvitationDTO> invitations = new ArrayList<>();
        invitations.add(new InvitationDTO(Invitation.Role.PARTICIPANTE, invitedUserEmail));
        //NOTA: otro posible bug sería que se pueda invitar al mismo participante dos veces pero con distinto rol
        invitations.add(new InvitationDTO(Invitation.Role.PARTICIPANTE, invitedUserEmail));

        NewSessionDTO dto = new NewSessionDTO(sessionTitle, sessionDescription, startTime, endTime, invitations, tags);
        sessionService.createSession(ownerEmail, dto);

        Optional<Session> session = sessionsRepository.findById(1L);
        ArrayList<Invitation> savedInvitations = new ArrayList<>(session.get().getInvitations());
        Assertions.assertThat(savedInvitations.size()).isEqualTo(2);
    }
}
