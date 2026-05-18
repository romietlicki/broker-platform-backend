package com.brokerplatform.controller;

import com.brokerplatform.dto.response.DelinquencyResponse;
import com.brokerplatform.dto.response.PolicyResponse;
import com.brokerplatform.security.UserPrincipal;
import com.brokerplatform.service.PolicyService;
import com.brokerplatform.service.insurer.InsurerType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/policies")
@RequiredArgsConstructor
@Tag(name = "Apólices", description = "Gerenciamento de apólices e clientes segurados")
@SecurityRequirement(name = "bearerAuth")
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping
    @Operation(summary = "Listar apólices do corretor logado",
               description = "Retorna lista paginada com filtros por status e nome do segurado")
    public ResponseEntity<Page<PolicyResponse>> listPolicies(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Filtrar por status: VIGENTE, PENDENTE, CANCELADA")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filtrar por nome do segurado")
            @RequestParam(required = false) String insuredName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "insuredName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(policyService.listPolicies(
                principal.getId(), status, insuredName, page, size, sortBy, sortDir));
    }

    @GetMapping("/{policyId}")
    @Operation(summary = "Detalhe de uma apólice")
    public ResponseEntity<PolicyResponse> getPolicy(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long policyId) {
        return ResponseEntity.ok(policyService.getPolicy(principal.getId(), policyId));
    }

    @GetMapping("/delinquencies")
    @Operation(summary = "Listar inadimplências",
               description = "Retorna todos os segurados com parcelas em atraso")
    public ResponseEntity<List<DelinquencyResponse>> listDelinquencies(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(policyService.listDelinquencies(principal.getId()));
    }

    @PostMapping("/sync")
    @Operation(summary = "Sincronizar apólices de todas as seguradoras")
    public ResponseEntity<Map<String, Object>> syncAll(
            @AuthenticationPrincipal UserPrincipal principal) {
        int count = policyService.syncAllPolicies(principal.getId());
        return ResponseEntity.ok(Map.of("synced", count, "message", "Sincronização concluída"));
    }

    @PostMapping("/sync/{insurer}")
    @Operation(summary = "Sincronizar apólices de uma seguradora específica",
               description = "Valores aceitos: ICATU, AZOS, MONGERAL")
    public ResponseEntity<Map<String, Object>> syncByInsurer(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String insurer) {
        InsurerType type = InsurerType.valueOf(insurer.toUpperCase());
        int count = policyService.syncPoliciesForInsurer(principal.getId(), type);
        return ResponseEntity.ok(Map.of("synced", count, "insurer", insurer.toUpperCase()));
    }
}
