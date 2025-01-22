package com.demokratica.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.demokratica.backend.Services.GPT_JWTService;

import static org.junit.jupiter.api.Assertions.*;

//TODO: entender cómo funciona Mockit, qué está haciendo aquí y por qué lo necesitamos
public class GPT_JWTServiceTest {

    @InjectMocks
    private GPT_JWTService jwtService;

    private String username;
    private String token;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        username = "testUser";
            token = jwtService.generateToken(username);
    }

    @Test
    void testGenerateToken() {
        String generatedToken = jwtService.generateToken(username);
        assertNotNull(generatedToken);
        assertTrue(generatedToken.length() > 0);
    }

    @Test
    void testGetUsernameFromToken() {
        String extractedUsername = jwtService.getUsernameFromToken(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void testValidateToken() {
        assertTrue(jwtService.validateToken(token));
    }

    /*@Test
    void testValidateTokenWithInvalidToken() {
        String invalidToken = "invalid.token.here";
        assertFalse(jwtService.validateToken(invalidToken));
    }
    */
}
