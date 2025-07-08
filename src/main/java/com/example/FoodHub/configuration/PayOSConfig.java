package com.example.FoodHub.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;
@Configuration
public class PayOSConfig {

    private String clientId = "841b8581-ba19-4778-851c-f54d277ab798";


    private String apiKey = "96921714-1926-4a72-affe-3e45a40719c9";


    private String checksumKey = "affbfa8d26634ea8ae2b6bbf26c162249396bbfcd83ca3ed94eaf2dd0a7023a1";

    @Bean
    PayOS payOS() {
        return new PayOS(clientId, apiKey, checksumKey);
    }
}
