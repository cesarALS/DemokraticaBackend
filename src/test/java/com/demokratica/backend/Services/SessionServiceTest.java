package com.demokratica.backend.Services;

import static org.junit.Assert.assertThrows;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.annotation.Rollback;

import com.demokratica.backend.Exceptions.InvalidInvitationsException;
import com.demokratica.backend.Exceptions.InvalidTagsException;
import com.demokratica.backend.Model.Invitation;
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
@MockitoSettings(strictness = Strictness.LENIENT)
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
    //Lo inicializo en el setup y no aquí para permitirme enviar tags repetidos (mismo texto) y probar que detecte el problema
    private ArrayList<TagDTO> tags;
    private ArrayList<InvitationDTO> invitationDTOs;
    private String ownerEmail = "owner@gmail.com";
    private String invitedUserEmail = "invitedUser@gmail.com";

    @BeforeEach
    public void setup() {
        tags = new ArrayList<>(List.of(new TagDTO("Prueba unitaria"), new TagDTO("Servicio de sesiones")));
        //La inicializamos vacía para que así cada test pueda poner las invitaciones que quiera
        invitationDTOs = new ArrayList<>();
        User owner = new User();
        owner.setEmail(ownerEmail);

        User invitedUser = new User();
        invitedUser.setEmail(invitedUserEmail);

        //Para que no haya problemas con la parte del servicio que detecta cuando un usuario no está registrado
        //TODO: Esto no debería ser necesario e indica que hay clases con excesivas responsabilidades, violando el principio de
        //Single Responsibility. La excesiva necesidad de usar Mocks demuestra un pobre diseño POO
        when(usersRepository.findById(ownerEmail)).thenReturn(Optional.of(owner));
        when(usersRepository.findById(invitedUserEmail)).thenReturn(Optional.of(invitedUser));
    }

    @Test
    @Order(1)
    @DisplayName("Prueba 1: invitar varias veces al mismo usuario")
    @Rollback(value = true)
    public void inviteSameUserTwice() {
        invitationDTOs.add(new InvitationDTO(Invitation.Role.PARTICIPANTE, invitedUserEmail));
        invitationDTOs.add(new InvitationDTO(Invitation.Role.PARTICIPANTE, invitedUserEmail));
        this.validateErrorDetection(invitationDTOs, InvalidInvitationsException.Type.INVITED_TWICE);
    }

    @Test
    @Order(2)
    @DisplayName("Prueba 2: invitar al mismo usuario con roles distintos")
    @Rollback(value = true)
    public void inviteSameUserDifferentRoles() {
        invitationDTOs.add(new InvitationDTO(Invitation.Role.PARTICIPANTE, invitedUserEmail));
        invitationDTOs.add(new InvitationDTO(Invitation.Role.EDITOR, invitedUserEmail));
        invitationDTOs.add(new InvitationDTO(Invitation.Role.ADMIN, invitedUserEmail));
        this.validateErrorDetection(invitationDTOs, InvalidInvitationsException.Type.INVITED_TWICE_DIFF_ROLE);
    }

    @Test
    @Order(3)
    @DisplayName("Prueba 3: invitar al dueño")
    @Rollback(value = true)
    public void inviteOwner() {
        invitationDTOs.add(new InvitationDTO(Invitation.Role.PARTICIPANTE, ownerEmail));
        this.validateErrorDetection(invitationDTOs, InvalidInvitationsException.Type.INVITED_OWNER);
    }

    @Test
    @Order(4)
    @DisplayName("Prueba 4: invitar a alguien con rol de dueño")
    @Rollback(value = true)
    public void inviteWithOwnerRole() {
        invitationDTOs.add(new InvitationDTO(Invitation.Role.DUEÑO, invitedUserEmail));
        this.validateErrorDetection(invitationDTOs, InvalidInvitationsException.Type.INVITED_ADDITIONAL_OWNER);
    }

    public void validateErrorDetection(ArrayList<InvitationDTO> invitationDTOs, InvalidInvitationsException.Type errorType) {
        NewSessionDTO dto = new NewSessionDTO(sessionTitle, sessionDescription, startTime, endTime, invitationDTOs, tags);
        InvalidInvitationsException e = assertThrows(InvalidInvitationsException.class, () -> sessionService.createSession(ownerEmail, dto));
        Assertions.assertThat(e.getErrorType()).isEqualTo(errorType);
    }

    @Test
    @Order(5)
    @DisplayName("Prueba 5: enviar tags repetidos (mismo texto)")
    @Rollback(value = true)
    public void sendDuplicatedTags() {
        //TODO: si mis excepciones personalizadas tuvieran un diseño POO más elegante sería posible hacer esto sin reduplicar tanto
        //código (últimas dos líneas)
        tags = new ArrayList<>(List.of(new TagDTO("Tag repetido"), new TagDTO("Tag repetido")));
        NewSessionDTO dto = new NewSessionDTO(sessionTitle, sessionDescription, startTime, endTime, invitationDTOs, tags);
        assertThrows(InvalidTagsException.class, () -> sessionService.createSession(ownerEmail, dto));
    }

    @Test
    @Order(6)
    @DisplayName("Prueba 6: actualizar invitados a una sesión y verificar el resultado")
    @Rollback(value = true)
    //TODO: 
    public void updateInvitedUsersTest() {
        //La idea es crear una primera sesión con un solo invitado (o sea dos participantes) y en la actualización no mandar ninguno
        //El resultado debería ser una lista con solo 1 participante (el dueño), si está funcionando correctamente
        ArrayList<InvitationDTO> invitationDTOs = new ArrayList<>();
        invitationDTOs.add(new InvitationDTO(Invitation.Role.PARTICIPANTE, invitedUserEmail));

        NewSessionDTO firstDto = new NewSessionDTO(sessionTitle, sessionDescription, startTime, endTime, invitationDTOs, tags);
        //Si pasó las pruebas anteriores no debería lanzar ninguna excepción aquí
        Session originalSession = sessionService.createSession(ownerEmail, firstDto);
        Assertions.assertThat(originalSession.getInvitations().size()).isEqualTo(2);

        when(sessionsRepository.findById(1L)).thenReturn(Optional.of(originalSession));

        //Ahora una lista vacía para la actualización. Solo debería quedar el dueño como invitado
        invitationDTOs = new ArrayList<>();
        NewSessionDTO updatedDto = new NewSessionDTO(sessionTitle, sessionDescription, startTime, endTime, invitationDTOs, tags);
        Session updatedSession = sessionService.updateSession(1L, ownerEmail, updatedDto);

        Assertions.assertThat(updatedSession.getInvitations().size()).isEqualTo(1);
    }

    @Test
    @Order(7)
    @DisplayName("Prueba 7: actualizar tags de una sesión y verificar el resultado")
    @Rollback(value = true)
    public void updateSessionTagsTest() {
        tags = new ArrayList<>(List.of(new TagDTO("Tag original")));
        NewSessionDTO firstDto = new NewSessionDTO(sessionTitle, sessionDescription, startTime, endTime, invitationDTOs, tags);
        //Si pasó las pruebas anteriores no debería lanzar ninguna excepción aquí
        Session originalSession = sessionService.createSession(ownerEmail, firstDto);
        Assertions.assertThat(originalSession.getTags().size()).isEqualTo(1);

        tags = new ArrayList<>(List.of(new TagDTO("Tag actualizado")));
        NewSessionDTO updatedDto = new NewSessionDTO(sessionTitle, sessionDescription, startTime, endTime, invitationDTOs, tags);
        Session updatedSession = sessionService.createSession(ownerEmail, updatedDto);
        //No debería aparecer el tag original, porque entonces significa que estamos añadiendo en lugar de actualizando
        Assertions.assertThat(updatedSession.getTags().size()).isEqualTo(1);
        Assertions.assertThat(updatedSession.getTags().iterator().next()).isEqualTo("Tag actualizado");
    }
}
