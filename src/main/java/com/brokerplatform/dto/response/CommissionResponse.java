package com.brokerplatform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CommissionResponse(
        String externalProposalId,
        String insuredName,
        String insurer,
        String productName,
        BigDecimal commissionAmount,
        BigDecimal commissionPercentage,
        LocalDate referenceMonth,
        String status,
        LocalDate paymentDate
) {}
