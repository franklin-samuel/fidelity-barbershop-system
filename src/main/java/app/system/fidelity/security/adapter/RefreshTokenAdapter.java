package app.system.fidelity.security.adapter;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.security.RefreshTokenPort;
import app.system.fidelity.domain.Jwt;
import app.system.fidelity.security.services.CustomUserDetailsService;
import app.system.fidelity.security.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenAdapter implements RefreshTokenPort {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Jwt execute(final Context context) {
        final String refreshToken = context.getProperty("refreshToken", String.class);

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BadCredentialsException("Refresh token is missing");
        }

        try {
            final String username = jwtUtil.extractUsername(refreshToken);

            final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!jwtUtil.isTokenValid(refreshToken, userDetails)) {
                throw new BadCredentialsException("Invalid refresh token");
            }

            final String newAccessToken = jwtUtil.generateAccessToken(userDetails);
            final String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

            return Jwt.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .type("Bearer")
                    .build();

        } catch (final ExpiredJwtException e) {
            throw new BadCredentialsException("Refresh token has expired");
        } catch (final JwtException e) {
            throw new BadCredentialsException("Invalid refresh token format");
        }
    }
}