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

    public String crearPreferenciaDePago(String planId) throws MPException, MPApiException {
        System.out.println(planId);
        // Configurar MercadoPago con el Access Token
        MercadoPagoConfig.setAccessToken("APP_USR-2595305637078013-020521-aeef252ee0e80a9800678340dce7dc81-2253827844");
        
         // Definir precios según el plan
        Map<String, BigDecimal> precios = new HashMap<>();
        precios.put("Plus", new BigDecimal(15000));
        precios.put("Premium", new BigDecimal(50000));
        precios.put("Profesional", new BigDecimal(120000));        

        if (!precios.containsKey(planId)) {
            throw new IllegalArgumentException("Plan no válido: " + planId);
        }


        // Crear una preferencia con el plan seleccionado
        PreferenceItemRequest item = PreferenceItemRequest.builder()
            .id(planId)
            .title("Plan " + planId.substring(0, 1).toUpperCase() + planId.substring(1))
            .quantity(1)
            .currencyId("COP")
            .unitPrice(precios.get(planId)) 
            .build();               

       
        //List<PreferenceItemRequest> items = new ArrayList<>();        

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
        .items(java.util.Collections.singletonList(item))
        .build();

        //PreferenceRequest preferenceRequest = PreferenceRequest.builder().items(items).build();

        PreferenceClient client = new PreferenceClient();     

        Preference preference = client.create(preferenceRequest);
        // Retornar la URL de pago
        return preference.getInitPoint();
    }
}

