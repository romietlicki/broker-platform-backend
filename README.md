# Broker Platform — Backend

## Pré-requisitos
- Java 17+
- Maven 3.9+
- MySQL 8.0+

## Setup rápido

### 1. Banco de dados
```sql
-- Execute o script de inicialização:
mysql -u root -p < src/main/resources/db/init.sql
```

### 2. Variáveis de ambiente (ou edite application.yml)
```bash
DB_USERNAME=root
DB_PASSWORD=sua_senha
JWT_SECRET=sua_chave_jwt_de_32_caracteres_minimo
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=seu@email.com
MAIL_PASSWORD=sua_senha_app
FRONTEND_URL=http://localhost:4200
ICATU_SUBSCRIPTION_KEY=sua_chave_icatu
ICATU_COMPANY_CODE=001
```

### 3. Executar em desenvolvimento
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Usuários de teste
| E-mail | Senha | Role |
|--------|-------|------|
| admin@brokerplatform.com | Admin@1234 | ROLE_ADMIN |
| corretor@brokerplatform.com | Broker@1234 | ROLE_BROKER |

> ⚠️ **Atenção**: A senha padrão nos dados de seed é apenas para desenvolvimento. Altere em produção.

## Swagger UI
Acesse em: `http://localhost:8080/api/swagger-ui.html`

## Endpoints principais

### Autenticação (público)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | /api/auth/login | Login |
| POST | /api/auth/register | Cadastro |
| POST | /api/auth/forgot-password | Solicitar reset de senha |
| POST | /api/auth/reset-password | Redefinir senha |

### Apólices (requer Bearer Token)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/policies | Listar com filtros e paginação |
| GET | /api/policies/{id} | Detalhe de uma apólice |
| GET | /api/policies/delinquencies | Listar inadimplentes |
| POST | /api/policies/sync | Sincronizar todas as seguradoras |
| POST | /api/policies/sync/{insurer} | Sincronizar seguradora específica |

### Comissionamento (requer Bearer Token)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/commissions | Comissionamento do mês atual |
| GET | /api/commissions/{insurer} | Comissionamento por seguradora |

## Seguradoras
- **ICATU**: Integração real (configure `ICATU_SUBSCRIPTION_KEY`)
- **AZOS**: Mock — retorna dados simulados
- **MONGERAL**: Mock — retorna dados simulados
