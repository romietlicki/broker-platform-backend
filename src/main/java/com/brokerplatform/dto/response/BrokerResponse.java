package com.brokerplatform.dto.response;

import com.brokerplatform.entity.Broker;

import java.time.LocalDateTime;
import java.util.List;

public record BrokerResponse(
        Long id,
        String uuid,
        String email,
        String fullName,
        String cpf,
        String susepCode,
        String phone,
        String addressStreet,
        String addressCity,
        String addressState,
        String addressZip,
        String status,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        List<String> roles
) {
    public static BrokerResponse from(Broker broker) {
        return new BrokerResponse(
                broker.getId(),
                broker.getUuid(),
                broker.getEmail(),
                broker.getFullName(),
                broker.getCpf(),
                broker.getSusepCode(),
                broker.getPhone(),
                broker.getAddressStreet(),
                broker.getAddressCity(),
                broker.getAddressState(),
                broker.getAddressZip(),
                broker.getStatus().name(),
                broker.getLastLoginAt(),
                broker.getCreatedAt(),
                broker.getRoles().stream().map(r -> r.getName()).toList()
        );
    }
}
