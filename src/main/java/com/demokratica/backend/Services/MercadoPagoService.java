package com.demokratica.backend.Services;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class MercadoPagoService {    
    @Value("${MP_ACCESS_TOKEN}")
    private String mpAccessToken;
    public String crearPreferenciaDePago(String planId, String email) throws MPException, MPApiException {
        System.out.println(planId);        
        MercadoPagoConfig.setAccessToken(mpAccessToken);        
        
         // Definir precios según el plan
        Map<String, BigDecimal> precios = new HashMap<>();
        precios.put("Plus", new BigDecimal(15000));
        precios.put("Premium", new BigDecimal(50000));
        precios.put("Profesional", new BigDecimal(120000));        

        if (!precios.containsKey(planId)) {
            return ("Plan " + planId + " no válido");
        }

        // Crear una preferencia de pago con el plan seleccionado
        PreferenceItemRequest item = PreferenceItemRequest.builder()
            .id(planId)
            .title("Plan " + planId.substring(0, 1).toUpperCase() + planId.substring(1))
            .quantity(1)
            .currencyId("COP")
            .unitPrice(precios.get(planId)) 
            .build();                           

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
        .items(java.util.Collections.singletonList(item))
        .binaryMode(true) // Solo habrá dos estados de pago: Aprobado, y no aprobado        
        .externalReference(email) // Asociar el email del usaurio a la transacción
        .build();        

        PreferenceClient client = new PreferenceClient();     

        Preference preference = client.create(preferenceRequest);        
        
        // Retornar la URL de pago
        return preference.getInitPoint();
    }

    public static final String WEBHOOK_VALIDO = "Webhook recibido";
    public static final String WEBHOOK_NO_AUTH = "No autorizado";    
    public static final String WEBHOOK_NOT_SUPPORTED_EVENT = "Evento no soportado";
    
    // Verificar la respuesta enviada por mercado pago cuando sucede un pago
    // Primer elemento: Respuesta de la api. Segundo elemento: id, o código de fallo
    public ArrayList<String> handleMercadoPagoWebhookRequest (MercadoPagoWebhookResponseDTO payload, String signature){            
        
        ArrayList<String> transactionValidity = new ArrayList<String>(2);
        
        if(!isValidSignature(signature)) {
            transactionValidity.add(WEBHOOK_NO_AUTH);
            transactionValidity.add(null);
        }

        transactionValidity.add(WEBHOOK_VALIDO);
        
        String eventType = (String) payload.type();
        String action = (String) payload.action();
        
        if("payment".equals(eventType) && "payment.updated".equals(action)){
            String id = payload.data().id();                                                
            transactionValidity.add(id);                                            
        } else transactionValidity.add(WEBHOOK_NOT_SUPPORTED_EVENT);
        
        return transactionValidity;
    }

    // TODO: Verificar la validez de la x-signature enviada por mercado pago para autenticarse
    private boolean isValidSignature(String signature){return true;}

    public record MercadoPagoUserIdDTO (String id){}
    
    public record MercadoPagoWebhookResponseDTO (String type, String action, MercadoPagoUserIdDTO data){}
}

