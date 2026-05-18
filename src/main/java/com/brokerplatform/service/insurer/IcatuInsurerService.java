package com.brokerplatform.service.insurer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class IcatuInsurerService implements InsurerService {

    private final WebClient webClient;
    private final String subscriptionKey;
    private final String companyCode;
    private final boolean enabled;

    public IcatuInsurerService(
            WebClient.Builder webClientBuilder,
            @Value("${app.insurer.icatu.base-url}") String baseUrl,
            @Value("${app.insurer.icatu.subscription-key}") String subscriptionKey,
            @Value("${app.insurer.icatu.company-code}") String companyCode,
            @Value("${app.insurer.icatu.enabled}") boolean enabled) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .defaultHeader("CodigoEmpresa", companyCode)
                .build();
        this.subscriptionKey = subscriptionKey;
        this.companyCode = companyCode;
        this.enabled = enabled;
    }

    @Override
    public InsurerType getType() {
        return InsurerType.ICATU;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PolicyData> fetchPolicies(String brokerCpfCnpj, String clientCpfCnpj) {
        log.info("[Icatu] Buscando apólices do cliente {} para corretor {}", clientCpfCnpj, brokerCpfCnpj);

        List<Map<String, Object>> response = webClient.get()
                .uri(uri -> uri
                        .path("/clientes/{idcliente}/apolices")
                        .queryParam("idcorretor", brokerCpfCnpj)
                        .build(clientCpfCnpj))
                .retrieve()
                .bodyToFlux(Map.class)
                .map(m -> (Map<String, Object>) m)
                .collectList()
                .block();

        if (response == null) return List.of();

        return response.stream().map(this::mapToPolicyData).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public PolicyData fetchPolicyDetails(String brokerCpfCnpj, String clientCpfCnpj,
                                         String policyId, String subId) {
        log.info("[Icatu] Buscando detalhes apólice {} subestipulante {}", policyId, subId);

        Map<String, Object> response = webClient.get()
                .uri(uri -> uri
                        .path("/clientes/{idcliente}/apolices/{idapolice}/subestipulantes/{idsub}")
                        .queryParam("idcorretor", brokerCpfCnpj)
                        .build(clientCpfCnpj, policyId, subId))
                .retrieve()
                .bodyToMono(Map.class)
                .map(m -> (Map<String, Object>) m)
                .block();

        if (response == null) return null;

        return mapDetailToPolicyData(response);
    }

    @Override
    public List<CommissionData> fetchCommissions(String brokerCpfCnpj, int month, int year) {
        log.info("[Icatu] Buscando comissionamento do corretor {} para {}/{}", brokerCpfCnpj, month, year);
        // Endpoint de comissionamento da Icatu – mapeado quando disponível
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private PolicyData mapToPolicyData(Map<String, Object> m) {
        String status = String.valueOf(m.getOrDefault("statusApolice", "DESCONHECIDO"));
        String startDateStr = String.valueOf(m.getOrDefault("inicioVigencia", ""));
        LocalDate startDate = startDateStr.isBlank() ? LocalDate.now() : LocalDate.parse(startDateStr);
        LocalDate cancellation = m.containsKey("dataCancelamentoApolice")
                ? LocalDate.parse(String.valueOf(m.get("dataCancelamentoApolice")))
                : null;

        List<Map<String, Object>> modules = (List<Map<String, Object>>) m.getOrDefault("modulos", List.of());
        String coverage = modules.stream()
                .map(mod -> String.valueOf(mod.getOrDefault("nome", "")))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        Object premioRaw = m.getOrDefault("premioTotal", 0);
        BigDecimal premio = new BigDecimal(String.valueOf(premioRaw));

        return new PolicyData(
                String.valueOf(m.getOrDefault("apolice", "")),
                "",
                "",
                normalizeStatus(status),
                startDate,
                null,
                List.of(),
                coverage,
                BigDecimal.ZERO,
                premio,
                "Mensal",
                String.valueOf(m.getOrDefault("produto", "")),
                null,
                cancellation
        );
    }

    @SuppressWarnings("unchecked")
    private PolicyData mapDetailToPolicyData(Map<String, Object> m) {
        String status = String.valueOf(m.getOrDefault("statusApolice", "DESCONHECIDO"));
        LocalDate startDate = m.containsKey("dataInicioVigencia")
                ? LocalDate.parse(String.valueOf(m.get("dataInicioVigencia")))
                : LocalDate.now();
        LocalDate endDate = m.containsKey("dataFimVigencia")
                ? LocalDate.parse(String.valueOf(m.get("dataFimVigencia")))
                : null;
        LocalDate cancellation = m.containsKey("dataCancelamentoApolice")
                ? LocalDate.parse(String.valueOf(m.get("dataCancelamentoApolice")))
                : null;

        Object premioRaw = m.getOrDefault("premioTotal", 0);
        BigDecimal premio = new BigDecimal(String.valueOf(premioRaw));

        Map<String, Object> capitalGlobal = (Map<String, Object>) m.getOrDefault("capitalGlobal", Map.of());
        BigDecimal capitalFuncionarios = new BigDecimal(String.valueOf(capitalGlobal.getOrDefault("capitalTotalFuncionarios", 0)));
        BigDecimal capitalSocios = new BigDecimal(String.valueOf(capitalGlobal.getOrDefault("capitalTotalSocios", 0)));
        BigDecimal capitalTotal = capitalFuncionarios.add(capitalSocios);

        Integer diaVencimento = m.containsKey("diaVencimento")
                ? Integer.parseInt(String.valueOf(m.get("diaVencimento")))
                : null;

        List<String> dueDates = diaVencimento != null
                ? buildDueDates(diaVencimento, startDate, endDate)
                : List.of();

        return new PolicyData(
                String.valueOf(m.getOrDefault("apolice", "")),
                String.valueOf(m.getOrDefault("nomeSubEstipulante", "")),
                String.valueOf(m.getOrDefault("cpfCnpjSubestipulante", "")),
                normalizeStatus(status),
                startDate,
                endDate,
                dueDates,
                "",
                capitalTotal,
                premio,
                String.valueOf(m.getOrDefault("periodicidadePagamento", "Mensal")),
                String.valueOf(m.getOrDefault("nomeProduto", "")),
                null,
                cancellation
        );
    }

    private String normalizeStatus(String status) {
        return switch (status.toLowerCase()) {
            case "ativo" -> "VIGENTE";
            case "cancelada", "cancelado" -> "CANCELADA";
            case "pendente" -> "PENDENTE";
            default -> status.toUpperCase();
        };
    }

    private List<String> buildDueDates(int day, LocalDate start, LocalDate end) {
        LocalDate limit = end != null ? end : start.plusYears(1);
        List<String> dates = new java.util.ArrayList<>();
        LocalDate current = start.withDayOfMonth(Math.min(day, start.lengthOfMonth()));
        while (!current.isAfter(limit)) {
            dates.add(current.toString());
            current = current.plusMonths(1);
            current = current.withDayOfMonth(Math.min(day, current.lengthOfMonth()));
        }
        return dates;
    }
}
