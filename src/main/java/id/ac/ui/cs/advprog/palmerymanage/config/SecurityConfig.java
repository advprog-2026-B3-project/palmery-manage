package id.ac.ui.cs.advprog.palmerymanage.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize on controllers
@RequiredArgsConstructor
@Profile("!dev")
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Microservice — tidak pakai CSRF (stateless REST)
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless: tidak ada session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Public: health check / actuator
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/debug/healthcheck").permitAll()

                        // GET kebun: ADMIN & MANDOR
                        .requestMatchers(HttpMethod.GET, "/kebun", "/kebun/**").hasAnyRole("ADMIN", "MANDOR")

                        // Semua mutating endpoint: ADMIN only
                        .requestMatchers(HttpMethod.POST, "/kebun/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/kebun/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/kebun/**").hasRole("ADMIN")

                        // Semua request lain wajib authenticated
                        .anyRequest().authenticated()
                )

                // Pasang JWT filter sebelum UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
