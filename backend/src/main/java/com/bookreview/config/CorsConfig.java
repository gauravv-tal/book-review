package com.bookreview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.frontend.url:http://dv8y18ytmxass.cloudfront.net}")
    private String frontendUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Build an allowlist for both HTTPS and HTTP variants of the frontend and ALB domains
        String httpVariant = frontendUrl.startsWith("https://")
            ? frontendUrl.replace("https://", "http://")
            : frontendUrl.startsWith("http://") ? frontendUrl : ("http://" + frontendUrl);
        String httpsVariant = frontendUrl.startsWith("http://")
            ? frontendUrl.replace("http://", "https://")
            : frontendUrl.startsWith("https://") ? frontendUrl : ("https://" + frontendUrl);

        // NOTE: Keep this list tight; these are the only allowed browser origins
        configuration.setAllowedOrigins(Arrays.asList(
            // CloudFront (both schemes)
            frontendUrl,   // as provided (http or https)
            httpVariant,   // explicit http variant
            httpsVariant,  // explicit https variant
            // ALB (both schemes)
            "http://book-review-alb-1256559597.ap-south-1.elb.amazonaws.com",
            "https://book-review-alb-1256559597.ap-south-1.elb.amazonaws.com",
            // Local development
            "http://localhost:3000",
            "https://localhost:3000"
        ));
        
        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials (for JWT tokens)
        configuration.setAllowCredentials(true);
        
        // Expose headers that the frontend might need
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With"
        ));
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
