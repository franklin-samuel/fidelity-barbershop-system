package app.system.fidelity.security.configuration;

import app.system.fidelity.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/**",
                                "/test/**"
                        ).permitAll()

                        // User
                        .requestMatchers("/user/**").hasAuthority("ADMIN")

                        // Settings
                        .requestMatchers(HttpMethod.GET, "/settings").hasAnyAuthority("ADMIN", "BARBER")
                        .requestMatchers("/settings/**").hasAuthority("ADMIN")

                        // Appointment
                        .requestMatchers(HttpMethod.GET, "/appointment/**").hasAnyAuthority("ADMIN", "BARBER")
                        .requestMatchers(HttpMethod.POST, "/appointment/**").hasAnyAuthority("ADMIN", "BARBER")
                        .requestMatchers(HttpMethod.PATCH, "/appointment/**").hasAnyAuthority("ADMIN", "BARBER")
                        .requestMatchers(HttpMethod.DELETE, "/appointment/**").hasAnyAuthority("ADMIN", "BARBER")

                        // Barber
                        .requestMatchers(HttpMethod.GET, "/barber/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/barber/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/barber/**").hasAuthority("ADMIN")

                        // Service
                        .requestMatchers(HttpMethod.GET, "/service/**").hasAnyAuthority("ADMIN", "BARBER")
                        .requestMatchers(HttpMethod.POST, "/service/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/service/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/service/**").hasAuthority("ADMIN")

                        // Product
                        .requestMatchers(HttpMethod.GET, "/product/**").hasAnyAuthority("ADMIN", "BARBER")
                        .requestMatchers(HttpMethod.POST, "/product/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/product/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/product/**").hasAuthority("ADMIN")

                        // Customer
                        .requestMatchers("/customer/**").hasAnyAuthority("ADMIN", "BARBER")

                        // Dashboard
                        .requestMatchers("/dashboard/**").hasAnyAuthority("ADMIN", "BARBER")

                        // Analytics
                        .requestMatchers("/analytics/**").hasAuthority("ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}