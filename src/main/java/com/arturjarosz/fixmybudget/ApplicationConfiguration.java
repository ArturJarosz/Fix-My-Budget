package com.arturjarosz.fixmybudget;

import com.arturjarosz.fixmybudget.properties.SecurityProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class ApplicationConfiguration {
    private static final String MATCH_ALL = "/**";

    private static final List<String> HEADERS =
            List.of(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, HttpHeaders.AUTHORIZATION, HttpHeaders.CACHE_CONTROL,
                    HttpHeaders.CONTENT_TYPE, HttpHeaders.SET_COOKIE, HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                    HttpHeaders.CONTENT_DISPOSITION);
    private static final List<String> API_METHODS =
            Arrays.asList(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PATCH.name(), HttpMethod.PUT.name(),
                    HttpMethod.DELETE.name());


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityProperties securityProperties)
            throws Exception {
        http.securityMatcher(MATCH_ALL) // Apply the security configuration to all endpoints
                .authorizeHttpRequests(authorize -> authorize.anyRequest()
                        .permitAll() // Permit all requests without authentication
                )
                .headers(headers -> headers.frameOptions(
                        HeadersConfigurer.FrameOptionsConfig::disable)) // to enable h2 console in browser
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource(
                        securityProperties))); // Enable CORS with the existing CorsConfigurationSource bean

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(SecurityProperties securityProperties) {
        log.info("Allowed CORS Origin domains: {}", securityProperties.allowedOrigins());
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(securityProperties.allowedOrigins());
        corsConfiguration.setAllowedHeaders(HEADERS);
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedMethods(API_METHODS);
        corsConfiguration.setExposedHeaders(HEADERS);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(MATCH_ALL, corsConfiguration);
        return source;
    }
}
