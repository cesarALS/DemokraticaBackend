/*package com.demokratica.backend.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

//TODO: entender cómo funciona Mockit, qué está haciendo aquí y por qué lo necesitamos
public class GPT_JWTServiceTest {

    @InjectMocks
    //@Autowired
    private JWTService jwtService;

    private String username;
    private String email;
    private String token;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        email = "arojasag@unal.edu.co";
        username = "Andres";
        token = jwtService.internalBuildToken(email, username);
    }

    @Test
    void testGenerateToken() {
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGetEmailFromToken() {
        String extractedEmail = jwtService.extractEmail(token);
        assertEquals(email, extractedEmail);
    }

    @Test
    void testValidateToken(UserService userService) {
        assertTrue(jwtService.validateToken(token, userService));
    }
*/
    /*@Test
    void testValidateTokenWithInvalidToken() {
        String invalidToken = "invalid.token.here";
        assertFalse(jwtService.validateToken(invalidToken));
    }
    */
//}
