package com.brokerplatform.controller;

import com.brokerplatform.dto.request.*;
import com.brokerplatform.dto.response.AuthResponse;
import com.brokerplatform.dto.response.BrokerResponse;
import com.brokerplatform.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de login, cadastro e redefinição de senha")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login do corretor",
               description = "Autentica o corretor e retorna tokens JWT de acesso e refresh")
    @ApiResponse(responseCode = "200", description = "Login bem-sucedido",
                 content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    @ApiResponse(responseCode = "423", description = "Conta bloqueada")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/register")
    @Operation(summary = "Cadastro de novo corretor",
               description = "Cria uma nova conta de corretor na plataforma")
    @ApiResponse(responseCode = "201", description = "Corretor cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já existente")
    public ResponseEntity<BrokerResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar redefinição de senha",
               description = "Envia e-mail com link para redefinição de senha")
    @ApiResponse(responseCode = "204", description = "E-mail enviado (ou e-mail não encontrado — sem revelação)")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha",
               description = "Redefine a senha usando o token recebido por e-mail")
    @ApiResponse(responseCode = "204", description = "Senha redefinida com sucesso")
    @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
