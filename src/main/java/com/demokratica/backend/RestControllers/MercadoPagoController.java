package com.demokratica.backend.RestControllers;

import com.demokratica.backend.Security.SecurityConfig;
import com.demokratica.backend.Services.MercadoPagoService;
import com.demokratica.backend.Services.UserService;
import com.mercadopago.net.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/payments")
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;
    private final UserService userService;

    public MercadoPagoController(MercadoPagoService mercadoPagoService, UserService userService) {
        this.mercadoPagoService = mercadoPagoService;
        this.userService = userService;
    }

    @PostMapping("/create")
public ResponseEntity<Map<String, String>> crearPreferenciaDePago(@RequestBody Map<String, String> requestBody) {
    try {
                
        String planId = requestBody.get("planId");

        if (planId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El planId es requerido"));
        }
        
        String userEmail = SecurityConfig.getUsernameFromAuthentication();                    

        // Crear una preferencia de pago, con el usuario asociado
        String urlPago = mercadoPagoService.crearPreferenciaDePago(planId, userEmail);

        return ResponseEntity.ok(Map.of("url", urlPago)); // Retornar JSON con la URL
    } catch (Exception e) {
        e.printStackTrace(); // Imprime la traza completa del error
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al procesar el pago"));
    }
}

    public static class PlanRequest {
        private String planId;

        public String getPlanId() {
            return planId;
        }

        public void setPlanId(String planId) {
            this.planId = planId;
        }
    }
}