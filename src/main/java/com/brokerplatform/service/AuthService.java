package com.brokerplatform.service;

import com.brokerplatform.dto.request.*;
import com.brokerplatform.dto.response.AuthResponse;
import com.brokerplatform.dto.response.BrokerResponse;
import com.brokerplatform.entity.Broker;
import com.brokerplatform.entity.BrokerSettings;
import com.brokerplatform.entity.LoginAudit;
import com.brokerplatform.entity.Role;
import com.brokerplatform.exception.BusinessException;
import com.brokerplatform.exception.ResourceNotFoundException;
import com.brokerplatform.repository.BrokerRepository;
import com.brokerplatform.repository.LoginAuditRepository;
import com.brokerplatform.repository.RoleRepository;
import com.brokerplatform.security.JwtTokenProvider;
import com.brokerplatform.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final BrokerRepository brokerRepository;
    private final RoleRepository roleRepository;
    private final LoginAuditRepository auditRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Broker broker = brokerRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Credenciais inválidas", HttpStatus.UNAUTHORIZED));

        if (broker.isLocked()) {
            auditLogin(broker, httpRequest, LoginAudit.LoginStatus.FAILED, "ACCOUNT_LOCKED");
            throw new LockedException("Conta bloqueada. Entre em contato com o suporte.");
        }

        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            broker.resetFailedAttempts();
            broker.setLastLoginAt(LocalDateTime.now());
            brokerRepository.save(broker);

            auditLogin(broker, httpRequest, LoginAudit.LoginStatus.SUCCESS, null);

            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            String accessToken = tokenProvider.generateAccessToken(auth);
            String refreshToken = tokenProvider.generateRefreshToken(auth);

            return AuthResponse.of(accessToken, refreshToken, principal.getId(),
                    broker.getFullName(), broker.getEmail(),
                    auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());

        } catch (BadCredentialsException e) {
            broker.incrementFailedAttempts();
            brokerRepository.save(broker);
            auditLogin(broker, httpRequest, LoginAudit.LoginStatus.FAILED, "INVALID_PASSWORD");
            throw new BusinessException("Credenciais inválidas", HttpStatus.UNAUTHORIZED);
        }
    }

    @Transactional
    public BrokerResponse register(RegisterRequest request) {
        if (brokerRepository.existsByEmail(request.email())) {
            throw new BusinessException("E-mail já cadastrado");
        }
        if (request.cpf() != null && brokerRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("CPF já cadastrado");
        }
        if (request.susepCode() != null && brokerRepository.existsBySusepCode(request.susepCode())) {
            throw new BusinessException("Código SUSEP já cadastrado");
        }

        Role brokerRole = roleRepository.findByName(Role.ROLE_BROKER)
                .orElseThrow(() -> new BusinessException("Role padrão não encontrada. Configure o banco de dados."));

        Broker broker = Broker.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .cpf(request.cpf())
                .susepCode(request.susepCode())
                .phone(request.phone())
                .addressStreet(request.addressStreet())
                .addressCity(request.addressCity())
                .addressState(request.addressState())
                .addressZip(request.addressZip())
                .status(Broker.Status.ACTIVE)
                .build();

        broker.getRoles().add(brokerRole);

        BrokerSettings settings = BrokerSettings.builder()
                .broker(broker)
                .build();
        broker.setSettings(settings);

        brokerRepository.save(broker);

        emailService.sendWelcomeEmail(broker.getEmail(), broker.getFullName());

        log.info("Novo corretor registrado: {}", broker.getEmail());
        return BrokerResponse.from(broker);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        brokerRepository.findByEmail(request.email()).ifPresent(broker -> {
            String token = UUID.randomUUID().toString().replace("-", "");
            broker.setPasswordResetToken(token);
            broker.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(1));
            brokerRepository.save(broker);
            emailService.sendPasswordResetEmail(broker.getEmail(), token, broker.getFullName());
            log.info("Token de redefinição enviado para: {}", broker.getEmail());
        });
        // Sempre retorna OK para não revelar se o e-mail existe ou não
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Broker broker = brokerRepository.findByPasswordResetToken(request.token())
                .orElseThrow(() -> new BusinessException("Token inválido ou expirado", HttpStatus.BAD_REQUEST));

        if (broker.getPasswordResetExpiresAt() == null ||
                broker.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Token expirado. Solicite uma nova redefinição.", HttpStatus.BAD_REQUEST);
        }

        broker.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        broker.setPasswordResetToken(null);
        broker.setPasswordResetExpiresAt(null);
        broker.resetFailedAttempts();
        brokerRepository.save(broker);

        log.info("Senha redefinida para corretor: {}", broker.getEmail());
    }

    private void auditLogin(Broker broker, HttpServletRequest request,
                            LoginAudit.LoginStatus status, String reason) {
        LoginAudit audit = LoginAudit.builder()
                .broker(broker)
                .ipAddress(extractIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .loginStatus(status)
                .failureReason(reason)
                .build();
        auditRepository.save(audit);
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
