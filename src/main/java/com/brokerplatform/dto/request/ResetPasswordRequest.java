package com.brokerplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres") String newPassword
) {}
