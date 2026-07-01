package com.algaworks.algashop.product.catalog.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ProductCatalogSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(
        sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(
        auth -> auth
                                .requestMatchers("/api/v1/products/*/withdraw", "/api/v1/products/*/restock").hasAuthority("SCOPE_products:stock:read")
                                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").hasAuthority("SCOPE_products:read")
                                .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAuthority("SCOPE_products:write")
                                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").hasAuthority("SCOPE_categories:read")
                                .requestMatchers(HttpMethod.POST, "/api/v1/categories/**").hasAuthority("SCOPE_categories:write")
                                .requestMatchers("/actuator/health").permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(Customizer.withDefaults())
                );

        return http.build();
    }

}
