package com.brokerplatform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendPasswordResetEmail(String toEmail, String token, String fullName) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Redefinição de Senha - Broker Platform");
        message.setText(String.format("""
                Olá, %s!

                Recebemos uma solicitação de redefinição de senha para sua conta.

                Clique no link abaixo para criar uma nova senha:
                %s

                Este link é válido por 1 hora.

                Se você não solicitou a redefinição, ignore este e-mail.

                Atenciosamente,
                Equipe Broker Platform
                """, fullName, resetLink));

        try {
            mailSender.send(message);
            log.info("E-mail de redefinição enviado para: {}", toEmail);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de redefinição para {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Bem-vindo à Broker Platform!");
        message.setText(String.format("""
                Olá, %s!

                Seu cadastro foi realizado com sucesso na Broker Platform.

                Acesse a plataforma em: %s

                Atenciosamente,
                Equipe Broker Platform
                """, fullName, frontendUrl));

        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de boas-vindas para {}: {}", toEmail, e.getMessage());
        }
    }
}
