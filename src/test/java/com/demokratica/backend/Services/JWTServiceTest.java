/*package com.demokratica.backend.Services;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JWTServiceTest {
    
    @Autowired
    private com.demokratica.backend.Services.GPT_JWTService jwtService;

    @Test
    @Order(1)
    @DisplayName("Prueba 1: crear token válido de prueba")
    public void createRealJWTTest() {
        //Inicializar JWT con null y luego assert que sea no nulo
        jwtService = null;

        Assertions.assertThat(jwtService).isNull();
    }

    @Test
    @Order(2)
    @DisplayName("Prueba 2: extraer payload del token válido de prueba")
    public void getJWTPayloadTest() {
        //Extraer el payload y assert que sea arojasag@unal.edu.co como email, arojasag como username y USER como authority
    }

    @Test
    @Order(3)
    @DisplayName("Prueba 3: verificar firma del token válido")
    public void validJwtSignatureTest() {
        //Assert que diga que es válido
    }

    @Test
    @Order(4)
    @DisplayName("Prueba 4: verificar firma de token falsificado")
    public void fakeJwtSignatureTest() {
        //Crear un token al azar y assert que diga que no es válido
    }

    @Test
    @Order(5)
    @DisplayName("Prueba 5: verificar que la validación tiene en cuenta la expiración")
    public void expiredJwtTest() {
        //Crear un token con fecha de expiración ya pasado y assert que diga es no es válido
    }
}
*/