package com.demokratica.backend.RestControllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/webhooks")
@CrossOrigin(origins = "*") // Toca deshabilitarlo completamente
public class WebhookControllers {
    
    // API que le permite a mercado pago notificar sobre el éxito o fracaso de la transacción
    // Esta api se asocia a mercado pago, en la página de mercado pago, con la cuenta de developer que uno tiene
    @PostMapping("/mercadopago")
    public ResponseEntity<String> handleWebhook(
        @RequestBody Map<String, Object> payload,
        @RequestHeader("x-signature") String signature //Así se autentica mercado pago
    ){
        try {
            if(!isValidSignature(signature, payload)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Firma no válida");
        
            String eventType = (String) payload.get("type");
            String action = (String) payload.get("action");

            if("payment".equals(eventType) && "payment.updated".equals(action)){
                // Aquí tomamos el email del usuario
                String userEmail = (String) payload.get("data.external_reference");

                String status = (String) payload.get("data.status");

                if("approved".equals(status)){
                    // TODO: Actualizar en la BD
                    System.out.println("Actualizar la bd de ".concat(userEmail));
                }
            }

            return ResponseEntity.ok("Webhook Recibido");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el webhook");
        }
    }

    // TODO: Verificar la validez de la x-signature de la api de mercado pago
    private boolean isValidSignature(String signature, Map<String, Object> payload){
        return true;
    }
}
