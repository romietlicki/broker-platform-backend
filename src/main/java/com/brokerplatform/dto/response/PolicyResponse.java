package com.brokerplatform.dto.response;

import com.brokerplatform.entity.PolicySnapshot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PolicyResponse(
        Long id,
        String externalProposalId,
        String insuredName,
        String insuredCpf,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        List<String> dueDates,
        String coverageDescription,
        BigDecimal insuredCapital,
        BigDecimal premiumTotal,
        String paymentPeriodicity,
        String insurer,
        String productName,
        Integer gracePeriodDays,
        LocalDate cancellationDate,
        LocalDateTime lastSyncedAt
) {
    public static PolicyResponse from(PolicySnapshot p) {
        return new PolicyResponse(
                p.getId(),
                p.getExternalProposalId(),
                p.getInsuredName(),
                p.getInsuredCpf(),
                p.getStatus(),
                p.getStartDate(),
                p.getEndDate(),
                p.getDueDates(),
                p.getCoverageDescription(),
                p.getInsuredCapital(),
                p.getPremiumTotal(),
                p.getPaymentPeriodicity(),
                p.getInsurer(),
                p.getProductName(),
                p.getGracePeriodDays(),
                p.getCancellationDate(),
                p.getLastSyncedAt()
        );
    }
}
