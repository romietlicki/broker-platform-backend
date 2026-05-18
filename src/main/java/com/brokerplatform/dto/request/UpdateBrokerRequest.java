package com.brokerplatform.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateBrokerRequest(
        @Size(max = 100) String fullName,
        @Pattern(regexp = "\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4}", message = "Telefone inválido") String phone,
        String addressStreet,
        String addressCity,
        @Size(max = 2) String addressState,
        @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP inválido") String addressZip
) {}
