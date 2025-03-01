package com.demokratica.backend.RestControllers;

import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demokratica.backend.Services.MercadoPagoService;
import com.demokratica.backend.Services.MercadoPagoService.MercadoPagoWebhookResponseDTO;

@RestController
@RequestMapping("/api/webhooks")
@CrossOrigin(origins = "*") // Toca deshabilitarlo completamente
public class WebhookController {
    
    private final MercadoPagoService mercadoPagoService;
    private final ObjectMapper objectMapper;    
    
    public WebhookController(MercadoPagoService mercadoPagoService, ObjectMapper objectMapper){
        this.mercadoPagoService = mercadoPagoService;
        this.objectMapper = new ObjectMapper();
    }
    
    // API que le permite a mercado pago notificar sobre el éxito o fracaso de la transacción
    // Esta api se asocia a mercado pago, en la página de mercado pago, con la cuenta de developer que uno tiene
    @PostMapping("/mercadopago")
    public ResponseEntity<String> handleWebhook(
        @RequestBody Map<String, Object> payload,
        @RequestHeader("x-signature") String signature //Así se autentica mercado pago
    ){
        try {
            
            MercadoPagoWebhookResponseDTO webhookPayload = objectMapper.convertValue(payload, MercadoPagoWebhookResponseDTO.class);

            ArrayList<String> transactionProcessing = mercadoPagoService.handleMercadoPagoWebhookRequest
                (webhookPayload, signature);

            if(transactionProcessing.get(0) == MercadoPagoService.WEBHOOK_NO_AUTH){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Firma no válida");
            }
            
            String id = transactionProcessing.get(1);

            if (id == MercadoPagoService.WEBHOOK_NOT_SUPPORTED_EVENT) System.out.println("Evento obtenido no válido");            
            else System.out.println("Id de pago: " + id);

             // Es esencial que mercado pago reciba un estatus de éxito, para que el pago se termine de procesar
            return ResponseEntity.ok("Webhook Recibido");
            
        } 
        catch (IllegalArgumentException e) {            
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error en el formato del JSON");             
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el webhook");
        }
    }
}
