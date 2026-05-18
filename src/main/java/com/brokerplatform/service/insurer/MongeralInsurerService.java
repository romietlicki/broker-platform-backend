package com.brokerplatform.service.insurer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class MongeralInsurerService implements InsurerService {

    private final boolean enabled;

    public MongeralInsurerService(@Value("${app.insurer.mongeral.enabled}") boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public InsurerType getType() {
        return InsurerType.MONGERAL;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<PolicyData> fetchPolicies(String brokerCpfCnpj, String clientCpfCnpj) {
        log.info("[Mongeral MOCK] Retornando apólices mockadas para {}", clientCpfCnpj);
        return List.of(
                new PolicyData(
                        "MAG-001",
                        "Ana Paula Ferreira",
                        clientCpfCnpj,
                        "VIGENTE",
                        LocalDate.of(2023, 9, 1),
                        LocalDate.of(2024, 9, 1),
                        List.of("2023-10-05", "2023-11-05", "2023-12-05",
                                "2024-01-05", "2024-02-05", "2024-03-05",
                                "2024-04-05", "2024-05-05", "2024-06-05",
                                "2024-07-05", "2024-08-05", "2024-09-05"),
                        "Plano de Previdência PGBL + Proteção por Morte Natural e Acidental",
                        new BigDecimal("300000.00"),
                        new BigDecimal("500.00"),
                        "Mensal",
                        "MAG Previdência Plus",
                        90,
                        null
                ),
                new PolicyData(
                        "MAG-002",
                        "Roberto Alves",
                        "987.654.321-00",
                        "CANCELADA",
                        LocalDate.of(2022, 1, 1),
                        LocalDate.of(2023, 1, 1),
                        List.of(),
                        "Seguro Vida em Grupo - Cobertura básica rescindida",
                        new BigDecimal("100000.00"),
                        new BigDecimal("120.00"),
                        "Mensal",
                        "MAG Empresarial Básico",
                        null,
                        LocalDate.of(2023, 3, 15)
                )
        );
    }

    @Override
    public PolicyData fetchPolicyDetails(String brokerCpfCnpj, String clientCpfCnpj,
                                         String policyId, String subId) {
        log.info("[Mongeral MOCK] Retornando detalhes mockados para apólice {}", policyId);
        return fetchPolicies(brokerCpfCnpj, clientCpfCnpj).stream()
                .filter(p -> p.externalProposalId().equals(policyId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<CommissionData> fetchCommissions(String brokerCpfCnpj, int month, int year) {
        log.info("[Mongeral MOCK] Retornando comissionamento mockado para {}/{}", month, year);
        return List.of(
                new CommissionData("MAG-001", "Ana Paula Ferreira", "MAG Previdência Plus",
                        new BigDecimal("50.00"), new BigDecimal("10.00"),
                        LocalDate.of(year, month, 1), "PAGO", LocalDate.of(year, month, 10)),
                new CommissionData("MAG-002", "Roberto Alves", "MAG Empresarial Básico",
                        new BigDecimal("0.00"), new BigDecimal("0.00"),
                        LocalDate.of(year, month, 1), "CANCELADO", null)
        );
    }
}
