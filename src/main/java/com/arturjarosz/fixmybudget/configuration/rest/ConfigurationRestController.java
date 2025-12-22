package com.arturjarosz.fixmybudget.configuration.rest;

import com.arturjarosz.fixmybudget.configuration.ConfigurationProvider;
import com.arturjarosz.fixmybudget.configuration.dto.ConfigurationResponse;
import com.arturjarosz.fixmybudget.properties.CategoryProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/configuration")
public class ConfigurationRestController {

    private final ConfigurationProvider configurationProvider;
    private final ObjectMapper objectMapper;
    private final CategoryProperties categoryProperties;

    @GetMapping
    ResponseEntity<ConfigurationResponse> getConfiguration() {
        return ResponseEntity.ok(this.configurationProvider.getConfigurationResponse());
    }

    @GetMapping("/download")
    ResponseEntity<byte[]> downloadConfiguration() throws JsonProcessingException {
        byte[] jsonBytes = this.objectMapper.writeValueAsBytes(this.categoryProperties);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"category-properties.json\"");
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, HttpHeaders.CONTENT_DISPOSITION);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBytes);
    }

}
