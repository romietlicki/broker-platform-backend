package com.brokerplatform.service.insurer;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CommissionData(
        String externalProposalId,
        String insuredName,
        String productName,
        BigDecimal commissionAmount,
        BigDecimal commissionPercentage,
        LocalDate referenceMonth,
        String status,
        LocalDate paymentDate
) {}
