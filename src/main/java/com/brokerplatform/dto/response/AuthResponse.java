package com.brokerplatform.dto.response;

import java.util.List;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        Long brokerId,
        String fullName,
        String email,
        List<String> roles
) {
    public static AuthResponse of(String accessToken, String refreshToken, Long brokerId,
                                  String fullName, String email, List<String> roles) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", 86400L,
                brokerId, fullName, email, roles);
    }
}
