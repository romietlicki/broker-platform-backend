package com.brokerplatform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DelinquencyResponse(
        Long policyId,
        String externalProposalId,
        String insuredName,
        String insuredCpf,
        BigDecimal premiumTotal,
        String overdueDate,
        BigDecimal insuredCapital,
        String insurer,
        String productName,
        String status
) {}
