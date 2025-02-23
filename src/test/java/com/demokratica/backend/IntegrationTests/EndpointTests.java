package com.demokratica.backend.IntegrationTests;

import static org.junit.Assert.assertThrows;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

import com.demokratica.backend.Exceptions.InvalidInvitationsException;
import com.demokratica.backend.Exceptions.InvalidTagsException;
import com.demokratica.backend.Exceptions.PollNotFoundException;
import com.demokratica.backend.Exceptions.SessionNotFoundException;
import com.demokratica.backend.Exceptions.UserNotFoundException;
import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Plan;
import com.demokratica.backend.Model.Poll;
import com.demokratica.backend.Model.Session;
import com.demokratica.backend.Model.User;
import com.demokratica.backend.Model.UserVote;
import com.demokratica.backend.Model.Invitation.InvitationStatus;
import com.demokratica.backend.Model.Invitation.Role;
import com.demokratica.backend.Repositories.InvitationsRepository;
import com.demokratica.backend.Repositories.PollOptionsRepository;
import com.demokratica.backend.Repositories.PollsRepository;
import com.demokratica.backend.Repositories.SessionsRepository;
import com.demokratica.backend.Repositories.UserVoteRepository;
import com.demokratica.backend.Repositories.UsersRepository;
import com.demokratica.backend.RestControllers.SessionController;
import com.demokratica.backend.RestControllers.SignupController;
import com.demokratica.backend.RestControllers.AccountController.UserDTO;
import com.demokratica.backend.RestControllers.ActivitiesController.CreatedPollResponse;
import com.demokratica.backend.RestControllers.ActivitiesController.NewPollDTO;
import com.demokratica.backend.RestControllers.ActivitiesController.VoteDTO;
import com.demokratica.backend.RestControllers.LoginController.LoginRequest;
import com.demokratica.backend.RestControllers.LoginController.LoginResponse;
import com.demokratica.backend.RestControllers.SessionController.InvitationDTO;
import com.demokratica.backend.RestControllers.SessionController.NewSessionDTO;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;
import com.demokratica.backend.RestControllers.SignupController.SignupData;
import com.demokratica.backend.Security.JwtAuthentication;
import com.demokratica.backend.RestControllers.AccountController;
import com.demokratica.backend.RestControllers.LoginController;
import com.demokratica.backend.Services.JWTService;
import com.demokratica.backend.Services.SessionService.GetSessionsDTO;

import jakarta.transaction.Transactional;
/*
 * Quiero que esto funcione como una prueba de integración de todos los endpoints de la aplicación.
 * Con eso me refiero a que pruebe desde el controlador hasta el repositorio (con una base de datos en memoria H2)
 * 
 * Hay que probar especialmente los endpoints para crear sesiones, actualizarlas, eliminarlas, leer todas las sesiones
 * de un usuario, leer todas las actividades de una sesión
 * Crear votaciones, eliminarlas, votar por una opción
 * 
 * Las de Registro, Login y gestión de cuenta por ahora son opcionales
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EndpointTests {

    //Controladores
    @Autowired
    SessionController sessionController;    //Espero que con esto baste para conectarse con el SessionService
    @Autowired
    SignupController signupController;
    @Autowired
    LoginController loginController;
    @Autowired
    AccountController accountController;
    
    //Servicios
    @Autowired
    JWTService jwtService;
    
    //Repositorios
    @Autowired
    UsersRepository usersRepository;
    @Autowired
    SessionsRepository sessionsRepository;
    @Autowired
    PollsRepository pollsRepository;
    @Autowired
    UserVoteRepository userVoteRepository;
    @Autowired
    InvitationsRepository invitationsRepository;

    @Autowired
    WebTestClient webTestClient;

    public SignupData getSignupData (int userNumber) {
        //No importa que la contraseña sea insegura porque solo es para hacer pruebas con una BD en memoria
        return new SignupData(getUserEmail(userNumber), getUsername(userNumber), "123");
    }

    public String getUserEmail (int userNumber) {
        return "test" + String.valueOf(userNumber) + "@gmail.com";
    }

    public String getUsername (int userNumber) {
        return "test" + String.valueOf(userNumber);
    }

    public String login (String userEmail) {
        LoginRequest request = new LoginRequest(userEmail, "123");
        LoginResponse response = webTestClient.post()
                        .uri("api/auth/login")
                        .bodyValue(request)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(LoginResponse.class)
                        .returnResult().getResponseBody();

        Assertions.assertThat(response).isNotNull();

        //Esto debería hacer que el usuario pase a estar autenticado con un JWT
        String jwtToken = response.jwtToken();
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(JwtAuthentication.authenticated(jwtToken));
        SecurityContextHolder.setContext(ctx);

        return jwtToken;
    }

    public void createInvalidSession (ArrayList<InvitationDTO> invalidInvitationDTOs) {
        login(getUserEmail(4));
        JwtAuthentication auth = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
        String jwtToken = (String) auth.getCredentials();

        NewSessionDTO newSessionDTO = new NewSessionDTOBuilder(4).
                setInvitationDTOs(invalidInvitationDTOs).
                build();

        String response = webTestClient.post()
                            .uri("/api/sessions")
                            .header("Authorization", "Bearer " + jwtToken)
                            .bodyValue(newSessionDTO)
                            .exchange()
                            .expectStatus().isBadRequest()
                            .expectBody(String.class)
                            .returnResult().getResponseBody();

        Assertions.assertThat(response).isNotNull();

        assertThrows(InvalidInvitationsException.class, () -> 
                sessionController.createNewSession(newSessionDTO));
    }

    public void updateSessionHelper (int userNumber, int sessionId) {
        NewSessionDTO newSessionDTO = new NewSessionDTOBuilder(sessionId)
                    .setTitle("Actualización sesión")
                    .build();

        String jwtToken = login(getUserEmail(userNumber));
        webTestClient.put()
                    .uri("/api/sessions/" + String.valueOf(sessionId))
                    .header("Authorization", "Bearer " + jwtToken)
                    .bodyValue(newSessionDTO)
                    .exchange()
                    .expectStatus().isForbidden();
    }

    //Try to create with the user no. [userNumber] a poll with number (used for
    //titles and descriptions) [pollNumber] in the session with id [sessionId]
    //This makes it possible to easily test different cases
    public CreatedPollResponse createPoll (int userNumber, int pollNumber, Long sessionId, boolean shouldSucceed) {
        String jwtToken = login(getUserEmail(userNumber));
        NewPollDTO newPollDTO = new NewPollDTOBuilder(pollNumber).build();

        ResponseSpec spec = webTestClient.post()
                .uri("/api/sessions/" + String.valueOf(sessionId) + "/polls")
                .header("Authorization", "Bearer " + jwtToken)
                .bodyValue(newPollDTO)
                .exchange();
        
        if (!shouldSucceed) {
            spec.expectStatus().isForbidden();
            return null;
        } else {
            CreatedPollResponse response = spec
                        .expectStatus().isCreated()
                        .expectBody(CreatedPollResponse.class)
                        .returnResult().getResponseBody();

            //En caso de que la función que nos llama quiera usar este resultado
            return response;
        }
    }

    public ResponseSpec voteInPollHelper(int userNumber, int pollId, long pollOptionId) {
        String jwtToken = login(getUserEmail(userNumber));

        ResponseSpec spec = webTestClient.post()
                .uri("/api/polls/" + String.valueOf(pollId))
                .header("Authorization", "Bearer " + jwtToken)
                .bodyValue(new VoteDTO(pollOptionId))
                .exchange();

        return spec;
    }

    public ResponseSpec acceptInvitationHelper (int userNumber, int sessionId) {
        String jwtToken = login(getUserEmail(userNumber));

        ResponseSpec spec = webTestClient.post()
                .uri("/api/sessions/" + String.valueOf(sessionId) + "/invitations/accept")
                .header("Authorization", "Bearer " + jwtToken)
                .exchange();

        return spec;
    }

    public void assertUserTotalVotes (int userNumber, long pollId, int expectedLength) {
        User user = usersRepository.findById(getUserEmail(userNumber)).orElseThrow(() ->
            new UserNotFoundException(getUserEmail(userNumber)));

        Poll poll = pollsRepository.findById(pollId).orElseThrow(() ->
            new PollNotFoundException(pollId));

        List<UserVote> userVotes = userVoteRepository.findByUserAndPoll(user, poll);
        Assertions.assertThat(userVotes.size()).isEqualTo(expectedLength);
    }


    //El usuario 4 y 5 se usarán para las pruebas relacionadas con crear sesiones inválidas
    @Test
    @DisplayName("Prueba 1: registrar cinco usuarios")
    @Order(1)
    @Rollback(value = false) //Para usar estos mismos usuarios para sesiones futuras
    public void shouldSignupFiveUsers() {
        for (int i = 1; i <= 5; i++) {
            signupController.signUp(getSignupData(i));

            String email = getUserEmail(i);
            User user = usersRepository.findById(email).orElseThrow(() -> 
                new UserNotFoundException(email));

            //Verifica que esté hasheando las contraseñas
            Assertions.assertThat(user.getPassword()).isNotEqualTo("123");
            Assertions.assertThat(user.getPlan().getId()).isEqualTo(i);
            Assertions.assertThat(user.getPlan().getPlanType()).isEqualTo(Plan.Type.GRATUITO);

            //Podría verificar que lo que está guardado coincida con su correo, nombre de usuario, que
            //sus invitaciones y votos estén vacíos, etc. No veo la necesidad    
        }
    }

    @Test
    @DisplayName("Prueba 2: crear tres sesiones")
    @Order(2)
    @Rollback(value = false) //Para poder usar estas mismas sesiones para pruebas siguientes
    @Transactional //Por razones que no entiendo si no la uso se cierra una "sesión" y no puedo acceder a las invitaciones de la sesión recién creada
    public void shouldCreateThreeSessions() {
        //La idea es que cada usuario sea dueño de una sesión y cada usuario esté en dos sesiones
        //Así puedo probar varias funcionalidades distintas, como no poder borrar sesiones de la que no se
        //es miembro, solo poder consultar las sesiones a las que ha sido invitado, etc.
        for (int i = 1; i <= 3; i++) {
            login(getUserEmail(i)); //Indirectamente probamos el endpoint para hacer login

            int participantId = (i == 3) ? 1 : i+1;
            ArrayList<InvitationDTO> invitationDTOs = new ArrayList<>();
            invitationDTOs.add(new InvitationDTO(Invitation.Role.PARTICIPANTE, getUserEmail(participantId)));
            NewSessionDTO newSessionDTO = new NewSessionDTOBuilder(i).
                                                setInvitationDTOs(invitationDTOs).
                                                build();

            sessionController.createNewSession(newSessionDTO);

            Long sessionId = Long.valueOf(i);
            Session createdSession = sessionsRepository.findById(sessionId).orElseThrow(() -> 
                new SessionNotFoundException(sessionId));

            Assertions.assertThat(createdSession.getInvitations().size()).isEqualTo(2);
        }

        Assertions.assertThat(sessionsRepository.count()).isEqualTo(3);
    }

    
    @Test
    @DisplayName("Prueba 3: crear sesión con tags repetidos")
    @Order(3)
    @Rollback(value = true) //En estas pruebas intento crear sesiones inválidas. Si las crea no quiero mantenerlas en la BD
    public void createSessionWithInvalidTags() {
        login(getUserEmail(4));
        ArrayList<TagDTO> tagDTOs = new ArrayList<>(List.of(new TagDTO("tag"), new TagDTO("tag")));
        NewSessionDTO newSessionDTO = new NewSessionDTOBuilder(4).setTagDTOs(tagDTOs).build();
        
        assertThrows(InvalidTagsException.class, () -> 
            sessionController.createNewSession(newSessionDTO));
    }

    @Test
    @DisplayName("Prueba 4: crear sesión inválida (invitar al dueño)")
    @Order(4)
    @Rollback(value = true)
    public void inviteOwnerToSession() {
        ArrayList<InvitationDTO> invitationDTOs = new ArrayList<>();
        invitationDTOs.add(new InvitationDTO(Role.DUEÑO, getUserEmail(4)));
        createInvalidSession(invitationDTOs);
    }

    @Test
    @DisplayName("Prueba 5: crear sesión inválida (invitar a un segundo dueño)")
    @Order(5)
    @Rollback(value = true)
    public void inviteAdditionalOwnerToSession() {
        ArrayList<InvitationDTO> invitationDTOs = new ArrayList<>();
        invitationDTOs.add(new InvitationDTO(Role.DUEÑO, getUserEmail(5)));
        createInvalidSession(invitationDTOs);
    }

    @Test
    @DisplayName("Prueba 6: crear sesión inválida (invitar al mismo usuario dos veces con el mismo rol)")
    @Order(6)
    @Rollback(value = true)
    public void inviteSameUserTwiceSameRole() {
        ArrayList<InvitationDTO> invitationDTOs = new ArrayList<>();
        invitationDTOs.add(new InvitationDTO(Role.PARTICIPANTE, getUserEmail(5)));
        invitationDTOs.add(new InvitationDTO(Role.PARTICIPANTE, getUserEmail(5)));
        createInvalidSession(invitationDTOs);
    }

    @Test
    @DisplayName("Prueba 7: crear sesión inválida (invitar al mismo usuario dos veces con distinto rol)")
    @Order(7)
    @Rollback(value = true)
    public void inviteSameUserTwiceDifferentRole() {
        ArrayList<InvitationDTO> invitationDTOs = new ArrayList<>();
        invitationDTOs.add(new InvitationDTO(Role.PARTICIPANTE, getUserEmail(5)));
        invitationDTOs.add(new InvitationDTO(Role.EDITOR, getUserEmail(5)));
        createInvalidSession(invitationDTOs);
    }

    

    @Test
    @DisplayName("Prueba 8: crear sesiones con tiempos no válidos")
    @Order(8)
    @Rollback(value = true)
    public void createSessionWithInvalidTimes() {
        login(getUserEmail(4));

        LocalDateTime startTime;
        LocalDateTime endTime;

        //La fecha de inicio es anterior a la de cierre
        startTime = LocalDateTime.now().minusHours(1);
        endTime = LocalDateTime.now();
        NewSessionDTO firstDTO = new NewSessionDTOBuilder(4)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .build();
        assertThrows(Exception.class, () -> 
            sessionController.createNewSession(firstDTO));
        
        //La sesión inicia y termina antes del periodo actual
        startTime = LocalDateTime.now().minusHours(2);
        endTime = LocalDateTime.now().minusHours(1);
        NewSessionDTO secondDTO = new NewSessionDTOBuilder(5)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .build();
        assertThrows(Exception.class, () ->
            sessionController.createNewSession(secondDTO));
    }

    @Test
    @DisplayName("Prueba 9: crear sesiones con valores nulos")
    @Order(9)
    @Rollback(value = true)
    public void createSessionWithNullValues() {
        //Fechas nulas, título nulo, descripción nula, tags nulos (no lista vacía, sino un null), invitaciones nulas, etc.
        for (int i = 0; i < 6; i++) {
            NewSessionDTOBuilder builder = new NewSessionDTOBuilder(5+i);
            switch (i) {
                case 0:
                    builder.setTitle(null);
                    break;
                case 1:
                    builder.setDescription(null);
                    break;
                case 2:
                    builder.setStartTime(null);
                    break;
                case 3:
                    builder.setEndTime(null);
                    break;
                case 4:
                    builder.setTagDTOs(null);
                    break;
                case 5:
                    builder.setInvitationDTOs(null);
                    break;
            }

            NewSessionDTO newSessionDTO = builder.build();

            assertThrows(RuntimeException.class, () ->
                sessionController.createNewSession(newSessionDTO));
        }
    }

    @Test
    @DisplayName("Prueba 10: borrar sesión inexistente")
    @Order(10)
    @Rollback(value = true)
    public void deleteInexistentSession() {
        login(getUserEmail(4));
        assertThrows(RuntimeException.class, () ->
            sessionController.deleteSession(4L));
    }

    @Test
    @DisplayName("Prueba 11: borrar sesión de la que NO se es dueño")
    @Order(11)
    @Rollback(value = true)
    public void deleteSessionNotOwned() {
        for (int i = 1; i <= 3; i++) {
            login(getUserEmail(i));

            Long sessionId = (i == 3) ? 1L : Long.valueOf(i) + 1;
            assertThrows(RuntimeException.class, () -> 
                sessionController.deleteSession(sessionId));
        }
    }

    @Test
    @DisplayName("Prueba 12: borrar sesión de la que SÍ se es dueño")
    @Order(12)
    @Rollback(value = true)
    @Transactional
    //Hay que verificar que todo lo asociado a la sesión haya sido borrado
    public void deleteSession() {        
        for (int i = 1; i <= 3; i++) {
            login(getUserEmail(i));
            Long sessionId = Long.valueOf(i);
            sessionController.deleteSession(sessionId);

            Assertions.assertThat(sessionsRepository.existsById(sessionId)).isFalse();
        }
    }

    @Test
    @DisplayName("Prueba 13: obtener sesiones de un usuario")
    @Order(13)
    @Rollback(value = false)
    public void getAllUserSessions() {
        for (int i = 1; i <=3; i++) {
            String jwtToken = login(getUserEmail(i));
            ArrayList<GetSessionsDTO> response = new ArrayList<>(webTestClient.get()
                            .uri("/api/sessions")
                            .header("Authorization", "Bearer " + jwtToken)
                            .exchange()
                            .expectStatus().isOk()
                            .expectBodyList(GetSessionsDTO.class)
                            .returnResult().getResponseBody());

            Assertions.assertThat(response.size()).isEqualTo(2);
            Assertions.assertThat(response.get(0).noActivities()).isEqualTo(0);
            Assertions.assertThat(response.get(0).noParticipants()).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Prueba 14: actualizar sesión inexistente")
    @Order(14)
    @Rollback(value = true)
    public void updateInexistentSession() {
        updateSessionHelper(1, 4);
    }

    @Test
    @DisplayName("Prueba 15: actualizar sesión sin haber sido invitado")
    @Order(15)
    @Rollback(value = true)
    public void updateSessionWithoutInvitation() {
        //El usuario 1 no fue invitado a la sesión 2 (la sesión entre los usuarios 2 y 3)
        updateSessionHelper(1, 2);
    }

    @Test
    @DisplayName("Prueba 16: actualizar sesión sin rol adecuado")
    @Order(16)
    @Rollback(value = true)
    public void updateSessionWithoutAdequateRole() {
        //El usuario 1 es tan solo participante en la sesión 3
        updateSessionHelper(1, 3);
    }

    @Test
    @DisplayName("Prueba 17: actualizar sesión (añadir invitados)")
    @Order(17)
    @Rollback(value = true)
    @Transactional
    public void updateSessionByAddingUsers() {
        //Vamos a hacer que la primera sesión, creada por el usuario 1, tenga 3 invitados en
        //lugar de 2
        login(getUserEmail(1));
        ArrayList<InvitationDTO> invitationDTOs = new ArrayList<>();
        invitationDTOs.add(new InvitationDTO(Role.PARTICIPANTE, getUserEmail(2)));
        invitationDTOs.add(new InvitationDTO((Role.PARTICIPANTE), getUserEmail(3)));

        NewSessionDTO newSessionDTO = new NewSessionDTOBuilder(1)
                .setInvitationDTOs(invitationDTOs)
                .build();

        sessionController.updateSession(1L, newSessionDTO);

        Session updatedSession = sessionsRepository.findById(1L).orElseThrow(() ->
            new SessionNotFoundException(1L));

        Assertions.assertThat(updatedSession.getInvitations().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("Prueba 18: actualizar sesión (añadir y quitar invitados)")
    @Order(18)
    @Rollback(value = true)
    @Transactional
    public void updateSessionByAddingAndRemovingUsers() {
        login(getUserEmail(2));
        ArrayList<InvitationDTO> invitationDTOs = new ArrayList<>();
        invitationDTOs.add(new InvitationDTO(Role.PARTICIPANTE, getUserEmail(1)));

        NewSessionDTO newSessionDTO = new NewSessionDTOBuilder(2)
                .setInvitationDTOs(invitationDTOs)
                .build();

        sessionController.updateSession(2L, newSessionDTO);

        Session updatedSession = sessionsRepository.findById(2L).orElseThrow(() ->
            new SessionNotFoundException(2L));

        Assertions.assertThat(updatedSession.getInvitations().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Prueba 19: leer nombre y correo de todos los usuarios en la BD")
    @Order(19)
    @Rollback(value = true)
    public void getAllUsers() {
        String jwtToken = login(getUserEmail(1));
        List<UserDTO> userDTOs = (webTestClient.get()
                .uri("/api/users")
                .header("Authorization", "Bearer " + jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDTO.class)
                .returnResult().getResponseBody());
          
        Assertions.assertThat(userDTOs).isNotNull();
        Assertions.assertThat(userDTOs.size()).isEqualTo(5);
        for (int i = 0; i < 5; i++) {
            String email = userDTOs.get(i).email();
            String username = userDTOs.get(i).username();

            //Porque los índices de la lista empiezan en 0 pero nuestros usuarios empiezan en 1
            Assertions.assertThat(email).isEqualTo(getUserEmail(i+1));
            Assertions.assertThat(username).isEqualTo(getUsername(i+1));
        }
    }

    @Test
    @DisplayName("Prueba 20: crear tres votaciones (una por sesión)")
    @Order(20)
    @Rollback(value = false)
    @Transactional
    public void createThreePolls() {
        for (int i = 1; i <= 3; i++) {
            Long sessionId = Long.valueOf(i);
            createPoll(i, i, sessionId, true);

            Session sessionWithPoll = sessionsRepository.findById(sessionId)
                    .orElseThrow(() -> new SessionNotFoundException(sessionId));

            Assertions.assertThat(sessionWithPoll.getPolls().size()).isEqualTo(1);
        }

        Assertions.assertThat(pollsRepository.count()).isEqualTo(3);
    }
    
    @Test
    @DisplayName("Prueba 21: crear votación en una sesión a la que no ha sido invitado")
    @Order(21)
    @Rollback(value = true)
    public void createPollWithoutSessionInvitation() {
        //Tratamos de crear una cuarta votación
        //El usuario 1 no ha sido invitado a la  sesión 2 así que debería fallar
        //con un error 403 Forbidden
        createPoll(1, 4, 2L, false);
    }

    @Test
    @DisplayName("Prueba 22: crear votación en una sesión inexistente")
    @Order(22)
    @Rollback(value = true)
    public void createPollInInexistentSession() {
        //Como solo existen 3 sesiones la sesión 4 no existe
        createPoll(1, 4, 4L, false);
    }

    @Test
    @DisplayName("Prueba 23: crear votación sin tener el rol necesario en la sesión")
    @Order(23)
    @Rollback(value = true)
    public void createPollWithoutAdequateRole() {
        //El usuario 2 fue invitado a la sesión 1 como participante, así que no
        //tiene el rol necesario para crear una votación en esa sesión
        createPoll(2, 4, 1L, false);
    }

    @Test
    @DisplayName("Prueba 24: votar sin haber sido invitado")
    @Order(24)
    @Rollback(value = true)
    public void voteWithoutInvitation() {
        //El usuario 1 no ha sido invitado a la sesión 2 y por tanto a la votación 2
        voteInPollHelper(1, 2, 1)
            .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Prueba 25: votar dos veces por diferentes opciones")
    @Order(25)
    @Rollback(value = true)
    @Transactional
    public void voteTwiceForDifferentOptions () {
        //Primera votación por el usuario 1 en la votación 1, creada por él mismo
        //Votamos por la primera opción
        voteInPollHelper(1, 1, 1)
                .expectStatus().isNoContent();

        //Ahora hacemos que el mismo usuario vote por la opción 2
        voteInPollHelper(1, 1, 2)
                .expectStatus().isNoContent();

        assertUserTotalVotes(1, 1L, 1);
    }

    @Test
    @DisplayName("Prueba 26: votar dos veces por la misma opción")
    @Order(26)
    @Rollback(value = true)
    @Transactional
    public void VoteTwiceForSameOption() {
        voteInPollHelper(1, 1, 1)
            .expectStatus().isNoContent();

        voteInPollHelper(1, 1, 1)
            .expectStatus().isNoContent();

        assertUserTotalVotes(1, 1L, 1);
    }

    @Test
    @DisplayName("Prueba 27: votar por una opción inexistente")
    @Order(27)
    @Rollback(value = true)
    public void voteForInexistentOption() {
        voteInPollHelper(1, 1, 4)
            .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Prueba 28: realizar votaciones válidas y comprobar resultados")
    @Order(28)
    @Rollback(value = true)
    public void voteInPolls() {
        //Cada usuario participa en la votación que creó (el 1 en la 1, el 2 en la 2, etc.) y
        //en la que fue invitado. El primer usuario vota por la primera opción, el segundo por la segunda, etc.
        for (int i = 1; i <= 3; i++) {
            int otherPollId = (i == 1) ? 3 : i-1;
            voteInPollHelper(i, i, Long.valueOf(i)).expectStatus().isNoContent();
            voteInPollHelper(i, otherPollId, Long.valueOf(i)).expectStatus().isNoContent();

            assertUserTotalVotes(i, i, 1);
            assertUserTotalVotes(i, Long.valueOf(otherPollId), 1);
        }
    }

    @Test
    @DisplayName("Prueba 29: aceptar invitación en sesión inexistente")
    @Order(29)
    @Rollback(true)
    public void acceptInvitationToInexistentSession() {
        //La sesión 4 no existe
        acceptInvitationHelper(1, 4).expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Prueba 30: aceptar invitación en sesión a la que no ha sido invitado")
    @Order(30)
    @Rollback(true)
    public void acceptInvitationToUninvitedSession() {
        //El primer usuario no fue invitado a la segunda sesión.
        acceptInvitationHelper(1, 2).expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Prueba 31: aceptar invitación ya previamente aceptada")
    @Order(31)
    @Rollback(true)
    public void acceptAlreadyAcceptedInvitation() {
        //Aceptamos dos veces la invitación a la sesión con id 3
        acceptInvitationHelper(1, 3).expectStatus().isNoContent();
        //El expectStatus parece no tener soporte para el código http Conflict así que 
        //toca poner el número 409 directamente
        acceptInvitationHelper(1, 3).expectStatus().isEqualTo(409);

        //Tratamos de aceptar la invitación a la sesión 1, ya automáticamente aceptada porque el usuario 1
        //es quien creó la sesión 1
        acceptInvitationHelper(1, 1).expectStatus().isEqualTo(409);
    }

    @Test
    //Un caso normal es que exista la sesión, el usuario haya sido invitado y la
    //solicitud siga pendiente
    @DisplayName("Prueba 32: aceptar invitación en casos normales")
    @Order(32)
    @Rollback(true)
    @Transactional
    public void acceptInvitationUsualCase() {
        for (int userNumber = 1; userNumber <= 3; userNumber++) {
            //El usuario 1 fue invitado a la sesión 3, el 2 a la sesión 1, el 3 a la sesión 2
            int invitedSessionId = (userNumber == 1) ? 3 : userNumber - 1;
            acceptInvitationHelper(userNumber, invitedSessionId).expectStatus().isNoContent();

            Optional<InvitationStatus> status = invitationsRepository
                .findInvitationStatusByEmailAndSessionId(getUserEmail(userNumber), Long.valueOf(invitedSessionId));

            Assertions.assertThat(status.isPresent()).isTrue();
            Assertions.assertThat(status.get()).isEqualTo(InvitationStatus.ACEPTADO);
        }
    }

    //No he añadido pruebas para hacer las mismas validaciones que se deben hacer
    //en las sesiones (tags repetidos, fechas inválidas, campos nulos, etc.)
    //No las hago porque debería primero buscar una manera más elegante para aplicar 
    //la misma validación de las sesiones en las votaciones comunes

    //Probar que no se pueda votar después de terminada la actividad
    //Hacer los casos de prueba necesarios para el caso de uso "borrar una votación"

    //También falta probar que no se pueda interactuar con una sesión de ninguna forma
    //sin antes haber aceptado la invitación

}
