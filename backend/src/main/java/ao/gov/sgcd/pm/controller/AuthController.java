package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.config.JwtTokenProvider;
import ao.gov.sgcd.pm.config.LoginRateLimiter;
import ao.gov.sgcd.pm.config.UserProperties;
import ao.gov.sgcd.pm.dto.AuthRequestDTO;
import ao.gov.sgcd.pm.dto.AuthResponseDTO;
import ao.gov.sgcd.pm.dto.RefreshTokenRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de autenticação: login, refresh e logout")
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserProperties userProperties;
    private final LoginRateLimiter rateLimiter;

    @Operation(summary = "Autenticar utilizador", description = "Realiza login com nome de utilizador e palavra-passe, devolvendo tokens JWT")
    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso")
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    @ApiResponse(responseCode = "429", description = "Demasiadas tentativas de login")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request,
                                                  HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        if (!rateLimiter.isAllowed(ip)) {
            return ResponseEntity.status(429).build();
        }

        var matched = userProperties.getUsers().stream()
                .filter(u -> u.getUsername().equals(request.getUsername()))
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPasswordHash()))
                .findFirst();

        if (matched.isEmpty()) {
            rateLimiter.recordFailedAttempt(ip);
            return ResponseEntity.status(401).build();
        }

        rateLimiter.resetAttempts(ip);
        String role = matched.get().getRole();
        String username = request.getUsername();
        String token = tokenProvider.generateToken(username, role);
        String refreshToken = tokenProvider.generateRefreshToken(username, role);

        return ResponseEntity.ok(AuthResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .role(role)
                .expiresIn(tokenProvider.getExpiration())
                .build());
    }

    @Operation(summary = "Renovar token de acesso", description = "Gera um novo token de acesso usando o refresh token")
    @ApiResponse(responseCode = "200", description = "Token renovado com sucesso")
    @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        var tokenData = tokenProvider.validateRefreshToken(request.getRefreshToken());
        if (tokenData.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        // Revoke old refresh token (rotation)
        tokenProvider.revokeRefreshToken(request.getRefreshToken());

        var data = tokenData.get();
        String newAccessToken = tokenProvider.generateToken(data.username(), data.role());
        String newRefreshToken = tokenProvider.generateRefreshToken(data.username(), data.role());

        return ResponseEntity.ok(AuthResponseDTO.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .role(data.role())
                .expiresIn(tokenProvider.getExpiration())
                .build());
    }

    @Operation(summary = "Terminar sessão", description = "Revoga o refresh token e invalida o token de acesso")
    @ApiResponse(responseCode = "200", description = "Sessão terminada com sucesso")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshTokenRequestDTO request,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (request != null && request.getRefreshToken() != null) {
            tokenProvider.revokeRefreshToken(request.getRefreshToken());
        }
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenProvider.blacklistToken(authHeader.substring(7));
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Obter utilizador autenticado", description = "Devolve o nome de utilizador e o papel (role) do utilizador autenticado")
    @ApiResponse(responseCode = "200", description = "Informações do utilizador devolvidas com sucesso")
    @ApiResponse(responseCode = "401", description = "Utilizador não autenticado")
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        String role = authentication.getAuthorities().iterator().next().getAuthority()
                .replace("ROLE_", "");
        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "role", role
        ));
    }
}
