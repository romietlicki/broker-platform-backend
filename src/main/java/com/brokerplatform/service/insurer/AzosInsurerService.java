package com.brokerplatform.service.insurer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class AzosInsurerService implements InsurerService {

    private final boolean enabled;

    public AzosInsurerService(@Value("${app.insurer.azos.enabled}") boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public InsurerType getType() {
        return InsurerType.AZOS;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<PolicyData> fetchPolicies(String brokerCpfCnpj, String clientCpfCnpj) {
        log.info("[Azos MOCK] Retornando apólices mockadas para {}", clientCpfCnpj);
        return List.of(
                new PolicyData(
                        "AZOS-001",
                        "Maria Oliveira",
                        clientCpfCnpj,
                        "VIGENTE",
                        LocalDate.of(2024, 3, 1),
                        LocalDate.of(2025, 3, 1),
                        List.of("2024-04-10", "2024-05-10", "2024-06-10", "2024-07-10",
                                "2024-08-10", "2024-09-10", "2024-10-10", "2024-11-10",
                                "2024-12-10", "2025-01-10", "2025-02-10", "2025-03-10"),
                        "Seguro de Vida Individual - Cobertura por Morte, Invalidez e Doenças Graves",
                        new BigDecimal("500000.00"),
                        new BigDecimal("350.00"),
                        "Mensal",
                        "Vida Individual Premium",
                        30,
                        null
                ),
                new PolicyData(
                        "AZOS-002",
                        "Carlos Mendes",
                        "123.456.789-00",
                        "PENDENTE",
                        LocalDate.of(2024, 6, 15),
                        LocalDate.of(2025, 6, 15),
                        List.of("2024-07-15", "2024-08-15"),
                        "Seguro de Vida Empresarial - Coberturas básicas",
                        new BigDecimal("200000.00"),
                        new BigDecimal("180.00"),
                        "Mensal",
                        "Vida Empresarial Essencial",
                        null,
                        null
                )
        );
    }

    @Override
    public PolicyData fetchPolicyDetails(String brokerCpfCnpj, String clientCpfCnpj,
                                         String policyId, String subId) {
        log.info("[Azos MOCK] Retornando detalhes mockados para apólice {}", policyId);
        return fetchPolicies(brokerCpfCnpj, clientCpfCnpj).stream()
                .filter(p -> p.externalProposalId().equals(policyId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<CommissionData> fetchCommissions(String brokerCpfCnpj, int month, int year) {
        log.info("[Azos MOCK] Retornando comissionamento mockado para {}/{}", month, year);
        return List.of(
                new CommissionData("AZOS-001", "Maria Oliveira", "Vida Individual Premium",
                        new BigDecimal("35.00"), new BigDecimal("10.00"),
                        LocalDate.of(year, month, 1), "PAGO", LocalDate.of(year, month, 15)),
                new CommissionData("AZOS-002", "Carlos Mendes", "Vida Empresarial Essencial",
                        new BigDecimal("18.00"), new BigDecimal("10.00"),
                        LocalDate.of(year, month, 1), "PENDENTE", null)
        );
    }
}
