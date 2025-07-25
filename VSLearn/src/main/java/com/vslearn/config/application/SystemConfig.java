package com.vslearn.config.application;

import com.vslearn.constant.ConstantVariables;
import com.vslearn.constant.UserRoles;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SystemConfig implements WebMvcConfigurer {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http
            .cors()
            .and()
            .authorizeHttpRequests(auth -> auth
                // Guest/public
                .requestMatchers("/api/v1/flashcards/**").permitAll()
                .requestMatchers("/api/v1/topics/list").permitAll()
                .requestMatchers("/api/v1/topics/all").permitAll()
                .requestMatchers("/api/v1/topics/{topicId}").permitAll()
                .requestMatchers("/api/v1/topics/status-options").permitAll()
                .requestMatchers("/api/v1/vocab/topics").permitAll()
                .requestMatchers("/api/v1/vocab/regions").permitAll()
                .requestMatchers("/api/v1/vocab/list").permitAll()
                .requestMatchers("/api/v1/vocab/{vocabId}").permitAll()

                // Authen endpoints (ai cũng gọi được)
                .requestMatchers("/users/signin").permitAll()
                .requestMatchers("/users/signup").permitAll()
                .requestMatchers("/users/signup/request-otp").permitAll()
                .requestMatchers("/users/signup/verify-otp").permitAll()
                .requestMatchers("/users/forgot-password").permitAll()
                .requestMatchers("/users/reset-password").permitAll()

                // General User (cần đăng nhập)
                .requestMatchers("/users/logout").hasAnyAuthority(UserRoles.GENERAL_USER, UserRoles.LEARNER, UserRoles.CONTENT_CREATOR, UserRoles.CONTENT_APPROVER, UserRoles.GENERAL_MANAGER)
                .requestMatchers("/users/profile/**").hasAnyAuthority(UserRoles.GENERAL_USER, UserRoles.LEARNER, UserRoles.CONTENT_CREATOR, UserRoles.CONTENT_APPROVER, UserRoles.GENERAL_MANAGER)
                .requestMatchers("/users/change-password").hasAnyAuthority(UserRoles.GENERAL_USER, UserRoles.LEARNER, UserRoles.CONTENT_CREATOR, UserRoles.CONTENT_APPROVER, UserRoles.GENERAL_MANAGER)

                // Learning path - cho phép guest user truy cập
                .requestMatchers("/api/v1/learning-path/**").permitAll()
                .requestMatchers("/api/v1/progress/**").permitAll()
                .requestMatchers("/api/test/**").permitAll()
                .requestMatchers("/api/v1/feedback/**").permitAll()

                // Content Creator
                .requestMatchers("/api/v1/vocab/create").hasAuthority(UserRoles.CONTENT_CREATOR)
                .requestMatchers("/api/v1/vocab/{vocabId}/update").hasAuthority(UserRoles.CONTENT_CREATOR)
                .requestMatchers("/api/v1/vocab/{vocabId}/delete").hasAuthority(UserRoles.CONTENT_CREATOR)
                .requestMatchers("/api/v1/topics/create").hasAuthority(UserRoles.CONTENT_CREATOR)
                .requestMatchers("/api/v1/topics/{topicId}/update").hasAuthority(UserRoles.CONTENT_CREATOR)
                .requestMatchers("/api/v1/topics/{topicId}/delete").hasAuthority(UserRoles.CONTENT_CREATOR)
                .requestMatchers("/api/v1/test-question/**").hasAuthority(UserRoles.CONTENT_CREATOR)

                // Content Approver
                .requestMatchers("/api/v1/approve/**").hasAuthority(UserRoles.CONTENT_APPROVER)

                // General Manager
                .requestMatchers("/api/v1/admin/**").hasAuthority(UserRoles.GENERAL_MANAGER)
                .requestMatchers("/api/v1/pricing/**").hasAuthority(UserRoles.GENERAL_MANAGER)
                .requestMatchers("/api/v1/revenue/**").hasAuthority(UserRoles.GENERAL_MANAGER)
                .requestMatchers("/api/v1/users/**").hasAuthority(UserRoles.GENERAL_MANAGER)
                .requestMatchers("/api/v1/support/**").hasAuthority(UserRoles.GENERAL_MANAGER)

                // Default: authenticated
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/users/signin")
                .defaultSuccessUrl("http://localhost:3000/oauth2/callback", true)
                .failureUrl("http://localhost:3000/login?error=oauth2_failed"))
            .csrf().disable();

        http.oauth2ResourceServer(oauth2 -> {
            oauth2.jwt(jwtConfigurer -> {
                jwtConfigurer.decoder(jwtDecoder());
                jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter());
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

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");
        grantedAuthoritiesConverter.setAuthorityPrefix(""); // Không thêm prefix vì token đã có ROLE_

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
    public Storage storage(@Value("${gcp.storage.credentials.location}") String credentialsPath) throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath.replace("file:", "")));
        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }
}