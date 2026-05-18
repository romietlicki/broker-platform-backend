package com.brokerplatform.controller;

import com.brokerplatform.dto.response.CommissionResponse;
import com.brokerplatform.security.UserPrincipal;
import com.brokerplatform.service.CommissionService;
import com.brokerplatform.service.insurer.InsurerType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commissions")
@RequiredArgsConstructor
@Tag(name = "Comissionamento", description = "Consulta de comissionamento do corretor")
@SecurityRequirement(name = "bearerAuth")
public class CommissionController {

    private final CommissionService commissionService;

    @GetMapping
    @Operation(summary = "Listar comissionamento",
               description = "Retorna o comissionamento do corretor para o mês/ano especificado (default: mês atual)")
    public ResponseEntity<List<CommissionResponse>> getCommissions(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Mês (1-12)") @RequestParam(required = false) Integer month,
            @Parameter(description = "Ano") @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(commissionService.getCommissions(principal.getId(), month, year));
    }

    @GetMapping("/{insurer}")
    @Operation(summary = "Comissionamento por seguradora",
               description = "Valores aceitos: ICATU, AZOS, MONGERAL")
    public ResponseEntity<List<CommissionResponse>> getByInsurer(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String insurer,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        InsurerType type = InsurerType.valueOf(insurer.toUpperCase());
        int m = month == 0 ? java.time.LocalDate.now().getMonthValue() : month;
        int y = year == 0 ? java.time.LocalDate.now().getYear() : year;
        return ResponseEntity.ok(commissionService.getCommissionsByInsurer(principal.getId(), type, m, y));
    }
}
