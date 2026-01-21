package app.system.fidelity.security.controller;


import app.system.fidelity.core.Context;
import app.system.fidelity.core.security.CreateTokenPort;
import app.system.fidelity.core.security.RefreshTokenPort;
import app.system.fidelity.domain.Jwt;
import app.system.fidelity.security.model.AuthRequest;
import app.system.fidelity.security.model.JwtResponse;
import app.system.fidelity.security.model.RefreshTokenRequest;
import app.system.fidelity.web.commons.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final CreateTokenPort createTokenPort;
    private final RefreshTokenPort refreshTokenPort;

    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<JwtResponse>> login(@RequestBody final AuthRequest authRequest) {

        final Context context = new Context();
        context.put("username", authRequest.username());
        context.put("password", authRequest.password());
        final Jwt jwt = createTokenPort.execute(context);

        JwtResponse data = JwtResponse.builder()
                .accessToken(jwt.getAccessToken())
                .refreshToken(jwt.getRefreshToken())
                .type(jwt.getType())
                .build();

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping(path = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@RequestBody final RefreshTokenRequest refreshTokenRequest) {
        final Context context = new Context();
        context.put("refreshToken", refreshTokenRequest.refreshToken());
        final Jwt jwt = refreshTokenPort.execute(context);

        JwtResponse data = JwtResponse.builder()
                .accessToken(jwt.getAccessToken())
                .refreshToken(jwt.getRefreshToken())
                .type(jwt.getType())
                .build();

        return ResponseEntity.ok(ApiResponse.success(data));
    }

}
