package com.brokerplatform.dto.request;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Email(message = "E-mail inválido") String email,
        @NotBlank @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres") String password,
        @NotBlank @Size(max = 100) String fullName,
        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF inválido (formato: 000.000.000-00)") String cpf,
        @Size(max = 20) String susepCode,
        @Pattern(regexp = "\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4}", message = "Telefone inválido") String phone,
        String addressStreet,
        String addressCity,
        @Size(max = 2) String addressState,
        @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP inválido (formato: 00000-000)") String addressZip
) {}
