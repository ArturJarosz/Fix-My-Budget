package com.arturjarosz.fixmybudget.configuration.rest;

import com.arturjarosz.fixmybudget.configuration.ConfigurationProvider;
import com.arturjarosz.fixmybudget.configuration.dto.ConfigurationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/configuration")
public class ConfigurationRestController {

    private final ConfigurationProvider configurationProvider;

    @GetMapping
    ResponseEntity<ConfigurationResponse> getConfiguration() {
        return ResponseEntity.ok(this.configurationProvider.getConfigurationResponse());
    }

}
