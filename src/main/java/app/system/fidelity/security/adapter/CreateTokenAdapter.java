package app.system.fidelity.security.adapter;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.security.CreateTokenPort;
import app.system.fidelity.domain.Jwt;
import app.system.fidelity.security.services.CustomUserDetailsService;
import app.system.fidelity.security.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CreateTokenAdapter implements CreateTokenPort {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Jwt execute(final Context context) {
        final var username = context.getProperty("username", String.class);
        final var password = context.getProperty("password", String.class);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        final var user = userDetailsService.loadUserByUsername(username);
        final var accessToken = jwtUtil.generateAccessToken(user);
        final var refreshToken = jwtUtil.generateRefreshToken(user);

        return Jwt.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .build();
    }
}