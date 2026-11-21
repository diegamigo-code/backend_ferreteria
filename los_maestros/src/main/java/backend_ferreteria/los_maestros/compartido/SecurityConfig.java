package backend_ferreteria.los_maestros.compartido;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Configuration
public class SecurityConfig {

    public static final String CLAIM_ROL = "rol";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint noAutenticado = (request, response, ex) ->
                escribirError(request, response, HttpStatus.UNAUTHORIZED,
                        "Debes iniciar sesión y enviar el token en el header Authorization: Bearer <token>");
        AccessDeniedHandler sinPermiso = (request, response, ex) ->
                escribirError(request, response, HttpStatus.FORBIDDEN, "No tienes permiso para esta operación");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()
                        .requestMatchers("/api/productos", "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers("/api/usuarios", "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/auditoria", "/api/auditoria/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/inventario", "/api/inventario/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/inventario").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/inventario/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/inventario/**").hasRole("ADMIN")
                        .requestMatchers("/api/inventario", "/api/inventario/**").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.GET, "/api/facturas/mias").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/facturas").authenticated()
                        .requestMatchers("/api/facturas", "/api/facturas/**").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.GET, "/api/credito/mio").hasRole("CONTRATISTA")
                        .requestMatchers("/api/credito", "/api/credito/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(noAutenticado)
                        .accessDeniedHandler(sinPermiso))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(noAutenticado)
                        .accessDeniedHandler(sinPermiso));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(CLAIM_ROL);
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        return converter;
    }

    private static void escribirError(HttpServletRequest request, HttpServletResponse response,
                                      HttpStatus status, String mensaje) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now()
                + "\",\"status\":" + status.value()
                + ",\"error\":\"" + status.getReasonPhrase()
                + "\",\"mensaje\":\"" + json(mensaje)
                + "\",\"ruta\":\"" + json(request.getRequestURI()) + "\"}");
    }

    private static String json(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
