package com.vslearn.config.application;

import com.vslearn.constant.ConstantVariables;
import com.nimbusds.jose.JWSAlgorithm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
public class SystemConfig implements WebMvcConfigurer {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http
            .cors()
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/learning-path/**").permitAll()
                .requestMatchers("/progress/**").permitAll()
                .requestMatchers("/demo/**").permitAll()
                .requestMatchers("/users/profile/**").hasAnyAuthority("SCOPE_ROLE_LEARNER")
                .requestMatchers("/users/subscription-status").hasAnyAuthority("SCOPE_ROLE_LEARNER")
                .requestMatchers("/users/signin").permitAll()
                .requestMatchers("/users/signup").permitAll()
                .requestMatchers("/users/signup/request-otp").permitAll()
                .requestMatchers("/users/signup/verify-otp").permitAll()
                .requestMatchers("/users/forgot-password").permitAll()
                .requestMatchers("/users/reset-password").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .anyRequest().authenticated())
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/users/signin")
                .defaultSuccessUrl("http://localhost:3000/oauth2/callback", true)
                .failureUrl("http://localhost:3000/login?error=true"))
            .csrf().disable();

        http.oauth2ResourceServer(oauth2 -> {
            oauth2.jwt(jwtConfigurer -> {
                jwtConfigurer.decoder(jwtDecoder());
            });
        });
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(ConstantVariables.SIGNER_KEY.getBytes(), JWSAlgorithm.HS256.toString());
        return NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}