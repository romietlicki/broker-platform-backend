package com.brokerplatform.service.insurer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InsurerServiceFactory {

    private final List<InsurerService> insurerServices;

    private Map<InsurerType, InsurerService> serviceMap() {
        return insurerServices.stream()
                .collect(Collectors.toMap(InsurerService::getType, Function.identity()));
    }

    public InsurerService get(InsurerType type) {
        InsurerService service = serviceMap().get(type);
        if (service == null) {
            throw new IllegalArgumentException("Seguradora não suportada: " + type);
        }
        return service;
    }

    public List<InsurerService> getAll() {
        return insurerServices;
    }

    public List<InsurerService> getEnabled() {
        return insurerServices.stream()
                .filter(InsurerService::isEnabled)
                .toList();
    }
}
