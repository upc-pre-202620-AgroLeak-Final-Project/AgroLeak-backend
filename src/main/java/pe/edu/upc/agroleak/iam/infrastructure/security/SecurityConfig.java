package pe.edu.upc.agroleak.iam.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import pe.edu.upc.agroleak.iam.domain.repository.UserRepository;
import pe.edu.upc.agroleak.common.exception.ApiError;

import java.time.Instant;
import java.util.*;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwt, UserRepository users, ObjectMapper mapper) throws Exception {
        org.springframework.security.web.AuthenticationEntryPoint unauthorized = (req, res, ex) -> {
            res.setStatus(401);
            res.setContentType("application/json");
            res.setHeader("WWW-Authenticate", "Bearer");
            mapper.writeValue(res.getOutputStream(), new ApiError(Instant.now(), 401, "Unauthorized", "Autenticación requerida o token inválido", req.getRequestURI(), Map.of()));
        };
        return http.csrf(c -> c.disable()).cors(org.springframework.security.config.Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.POST, "/api/v1/iam/auth/register", "/api/v1/iam/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/actuator/health", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("ADMIN", "FARMER")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("ADMIN", "FARMER")
                        .requestMatchers(HttpMethod.PATCH, "/api/**").hasAnyRole("ADMIN", "FARMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasAnyRole("ADMIN", "FARMER")
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint(unauthorized).accessDeniedHandler((req, res, ex) -> {
                    res.setStatus(403);
                    res.setContentType("application/json");
                    mapper.writeValue(res.getOutputStream(), new ApiError(Instant.now(), 403, "Forbidden", "Operación no permitida", req.getRequestURI(), Map.of()));
                }))
                .oauth2ResourceServer(o -> o.authenticationEntryPoint(unauthorized).jwt(j -> j.decoder(jwt::decode)
                        .jwtAuthenticationConverter(token -> {
                            UUID id;
                            try {
                                id = UUID.fromString(token.getSubject());
                            } catch (Exception ex) {
                                throw new OAuth2AuthenticationException("invalid_token");
                            }
                            var user = users.findById(id).filter(u -> u.isActive())
                                    .orElseThrow(() -> new OAuth2AuthenticationException("invalid_token"));
                            return new JwtAuthenticationToken(token, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
                        }))).build();
    }
}
