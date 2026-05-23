package id.ac.ui.cs.advprog.palmerymanage.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
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
                .cors(Customizer.withDefaults())

                // Microservice — tidak pakai CSRF (stateless REST)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())

                // Stateless: tidak ada session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public: health check / actuator
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/debug/healthcheck").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/harvests/photos/**").permitAll()

                        // Allow CORS preflight from the FE before authenticated requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // GET kebun: read-only access for setup/review and Buruh harvest submission
                        .requestMatchers(HttpMethod.GET, "/kebun", "/kebun/**").hasAnyRole("ADMIN", "MANDOR", "BURUH")

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
