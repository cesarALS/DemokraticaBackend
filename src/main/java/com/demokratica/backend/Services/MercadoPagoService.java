package com.demokratica.backend.Services;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class MercadoPagoService {    

    public String crearPreferenciaDePago(String planId, String email) throws MPException, MPApiException {
        System.out.println(planId);
        
        MercadoPagoConfig.setAccessToken("APP_USR-2595305637078013-020521-aeef252ee0e80a9800678340dce7dc81-2253827844");        
        
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
}

