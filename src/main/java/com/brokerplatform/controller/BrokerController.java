package com.brokerplatform.controller;

import com.brokerplatform.dto.request.UpdateBrokerRequest;
import com.brokerplatform.dto.response.BrokerResponse;
import com.brokerplatform.security.UserPrincipal;
import com.brokerplatform.service.BrokerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/brokers")
@RequiredArgsConstructor
@Tag(name = "Corretor", description = "Gerenciamento de perfil do corretor logado")
@SecurityRequirement(name = "bearerAuth")
public class BrokerController {

    private final BrokerService brokerService;

    @GetMapping("/me")
    @Operation(summary = "Perfil do corretor logado")
    public ResponseEntity<BrokerResponse> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(brokerService.getById(principal.getId()));
    }

    @PatchMapping("/me")
    @Operation(summary = "Atualizar perfil do corretor logado")
    public ResponseEntity<BrokerResponse> updateMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateBrokerRequest request) {
        return ResponseEntity.ok(brokerService.update(principal.getId(), request));
    }
}
