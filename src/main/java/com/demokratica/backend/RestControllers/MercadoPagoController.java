package com.demokratica.backend.RestControllers;

import com.demokratica.backend.Services.MercadoPagoService;
import com.mercadopago.net.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/payments")
//TODO: hay que corregir esto porque prácticamente deshabilita la protección CORS por completo
//Lo dejo aquí para que los chicos del lab de cripto tengan algo a lo que hacerle un exploit, si es posible
@CrossOrigin(origins = "*") // Permitir llamadas desde el frontend
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;

    public MercadoPagoController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    @PostMapping("/create")
public ResponseEntity<Map<String, String>> crearPreferenciaDePago(@RequestBody Map<String, String> requestBody) {
    try {
        String planId = requestBody.get("planId");

        if (planId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El planId es requerido"));
        }

        String urlPago = mercadoPagoService.crearPreferenciaDePago(planId); // Modifica el servicio para recibir planId

        return ResponseEntity.ok(Map.of("url", urlPago)); // Retornar JSON con la URL
    } catch (Exception e) {
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