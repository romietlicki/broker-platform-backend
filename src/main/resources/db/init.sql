-- ============================================================
-- Broker Platform - Script de Inicialização do Banco de Dados
-- MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS broker_platform
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE broker_platform;

-- 1. TABELA PRINCIPAL DO CORRETOR/USUÁRIO
CREATE TABLE IF NOT EXISTS broker (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid                     VARCHAR(36)  UNIQUE NOT NULL DEFAULT (UUID()),
    email                    VARCHAR(150) UNIQUE NOT NULL,
    password_hash            VARCHAR(255) NOT NULL,
    full_name                VARCHAR(100) NOT NULL,
    cpf                      VARCHAR(14)  UNIQUE,
    susep_code               VARCHAR(20)  UNIQUE,
    phone                    VARCHAR(20),
    address_street           VARCHAR(200),
    address_city             VARCHAR(100),
    address_state            CHAR(2),
    address_zip              VARCHAR(10),
    status                   ENUM('ACTIVE','INACTIVE','SUSPENDED') DEFAULT 'ACTIVE',
    last_login_at            TIMESTAMP NULL,
    failed_login_attempts    INT          DEFAULT 0,
    is_locked                BOOLEAN      DEFAULT FALSE,
    password_reset_token     VARCHAR(100),
    password_reset_expires_at TIMESTAMP   NULL,
    created_at               TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email   (email),
    INDEX idx_status  (status),
    INDEX idx_susep   (susep_code),
    INDEX idx_pwr_token (password_reset_token)
);

-- 2. TABELA DE PERFIS/ROLES
CREATE TABLE IF NOT EXISTS role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(100)
);

-- 3. RELACIONAMENTO N:N USUÁRIO ↔ PERFIL
CREATE TABLE IF NOT EXISTS broker_role (
    broker_id BIGINT NOT NULL,
    role_id   BIGINT NOT NULL,
    PRIMARY KEY (broker_id, role_id),
    FOREIGN KEY (broker_id) REFERENCES broker(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id)   REFERENCES role(id)   ON DELETE CASCADE
);

-- 4. PREFERÊNCIAS/CONFIGURAÇÕES DO USUÁRIO
CREATE TABLE IF NOT EXISTS broker_settings (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    broker_id           BIGINT  UNIQUE NOT NULL,
    language            VARCHAR(10)  DEFAULT 'pt-BR',
    timezone            VARCHAR(50)  DEFAULT 'America/Sao_Paulo',
    notifications_email BOOLEAN      DEFAULT TRUE,
    notifications_push  BOOLEAN      DEFAULT FALSE,
    dashboard_theme     VARCHAR(20)  DEFAULT 'light',
    auto_sync_policies  BOOLEAN      DEFAULT TRUE,
    FOREIGN KEY (broker_id) REFERENCES broker(id) ON DELETE CASCADE
);

-- 5. AUDITORIA DE LOGIN/SESSÕES
CREATE TABLE IF NOT EXISTS login_audit (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    broker_id      BIGINT NOT NULL,
    ip_address     VARCHAR(45),
    user_agent     VARCHAR(255),
    login_status   ENUM('SUCCESS','FAILED') NOT NULL,
    failure_reason VARCHAR(100),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (broker_id) REFERENCES broker(id) ON DELETE CASCADE,
    INDEX idx_broker_login (broker_id, created_at DESC)
);

-- 6. CACHE LOCAL DE APÓLICES
CREATE TABLE IF NOT EXISTS policy_snapshot (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    broker_id             BIGINT        NOT NULL,
    external_proposal_id  VARCHAR(100)  NOT NULL,
    insured_name          VARCHAR(150)  NOT NULL,
    insured_cpf           VARCHAR(14),
    status                VARCHAR(50)   NOT NULL,
    start_date            DATE          NOT NULL,
    end_date              DATE,
    due_dates             JSON          NOT NULL,
    coverage_description  TEXT,
    insured_capital       DECIMAL(15,2),
    premium_total         DECIMAL(15,2),
    payment_periodicity   VARCHAR(30),
    insurer               VARCHAR(30),
    product_name          VARCHAR(150),
    grace_period_days     INT,
    cancellation_date     DATE,
    last_synced_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (broker_id) REFERENCES broker(id) ON DELETE CASCADE,
    UNIQUE KEY uq_broker_external (broker_id, external_proposal_id),
    INDEX idx_broker_active (broker_id, last_synced_at DESC),
    INDEX idx_status (status)
);

-- ============================================================
-- DADOS INICIAIS
-- ============================================================

-- Roles padrão
INSERT IGNORE INTO role (name, description) VALUES
    ('ROLE_BROKER',  'Corretor de seguros padrão'),
    ('ROLE_ADMIN',   'Administrador da plataforma'),
    ('ROLE_MANAGER', 'Gestor de equipe de corretores');

-- Usuário Admin padrão (senha: Admin@1234)
-- Hash BCrypt strength=12 para 'Admin@1234'
INSERT IGNORE INTO broker (uuid, email, password_hash, full_name, cpf, susep_code, status)
VALUES (
    UUID(),
    'admin@brokerplatform.com',
    '$2a$12$LqOInvUICiB8Q.0lHBhb2uPGQfhEtLQRSxD9RJV4n6TZN1vvxMZ0q',
    'Administrador',
    '000.000.000-00',
    'ADMIN-001',
    'ACTIVE'
);

-- Associar admin à role ROLE_ADMIN
INSERT IGNORE INTO broker_role (broker_id, role_id)
SELECT b.id, r.id
FROM broker b, role r
WHERE b.email = 'admin@brokerplatform.com'
  AND r.name = 'ROLE_ADMIN';

-- Settings padrão para o admin
INSERT IGNORE INTO broker_settings (broker_id)
SELECT id FROM broker WHERE email = 'admin@brokerplatform.com';

-- ============================================================
-- CORRETOR DE TESTE (senha: Broker@1234)
-- Hash BCrypt strength=12 para 'Broker@1234'
-- ============================================================
INSERT IGNORE INTO broker (uuid, email, password_hash, full_name, cpf, susep_code, phone, status)
VALUES (
    UUID(),
    'corretor@brokerplatform.com',
    '$2a$12$LqOInvUICiB8Q.0lHBhb2uPGQfhEtLQRSxD9RJV4n6TZN1vvxMZ0q',
    'João Corretor Silva',
    '123.456.789-09',
    'SUSEP-12345',
    '(11) 99999-8888',
    'ACTIVE'
);

INSERT IGNORE INTO broker_role (broker_id, role_id)
SELECT b.id, r.id
FROM broker b, role r
WHERE b.email = 'corretor@brokerplatform.com'
  AND r.name = 'ROLE_BROKER';

INSERT IGNORE INTO broker_settings (broker_id)
SELECT id FROM broker WHERE email = 'corretor@brokerplatform.com';
