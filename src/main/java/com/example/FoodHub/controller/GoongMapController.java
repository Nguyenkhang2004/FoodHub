package com.example.FoodHub.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/goong")
public class GoongMapController {
    @Value("${goong.api.key}")
    private String goongApiKey;

    private final WebClient webClient;

    public GoongMapController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://rsapi.goong.io").build();
    }

    @GetMapping(value = "/autocomplete", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getAddressSuggestions(@RequestParam("input") String input) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/Place/AutoComplete")
                        .queryParam("api_key", goongApiKey)
                        .queryParam("input", input)
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }
}