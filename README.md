# Painel de Leads (Java + Spring Boot)

Sistema para a equipe da agência cadastrar e acompanhar leads, com login, funil em Kanban,
prioridade automática (lead scoring), painel de indicadores e exportação em CSV.

**Stack:** Java 17, Spring Boot 3.3, Spring Security (login por formulário, senhas com BCrypt),
Spring Data JPA, Thymeleaf, PostgreSQL (H2 para testes locais).

## Rodar localmente (sem instalar banco)

Requisitos: JDK 17+ e Maven 3.9+.

    mvn spring-boot:run

Acesse http://localhost:8080 e entre com `admin@agencia.com` / `trocar123`.
Sem variáveis de ambiente, os dados ficam em `./data` (H2). Troque a senha do admin em **Usuários**.

## Rodar com Docker + PostgreSQL

    cp .env.example .env   # edite as senhas
    docker compose up -d --build

## Variáveis de ambiente

| Variável | Para que serve | Padrão |
|---|---|---|
| DB_URL | URL JDBC (ex.: jdbc:postgresql://host:5432/leads) | H2 em arquivo |
| DB_USER / DB_PASSWORD | Credenciais do banco | sa / vazio |
| ADMIN_EMAIL / ADMIN_SENHA / ADMIN_NOME | Admin criado na primeira execução | admin@agencia.com / trocar123 |
| PORT | Porta HTTP | 8080 |

## Perfis

- **Administrador:** tudo, mais a tela *Usuários* (criar, desativar, trocar senha).
- **Usuário:** cadastra e edita leads.

## Pontuação de prioridade

Orçamento ≥ R$ 10 mil (+30) ou ≥ R$ 5 mil (+15); prazo < 30 dias (+20); indicação (+20); decisor (+15);
já respondeu (+10); 7+ dias sem contato em lead aberto (−15). Alta ≥ 70, média 40–69, baixa < 40.
A regra fica em `Lead.getPontuacao()`. A prioridade também pode ser definida manualmente.

## Antes de ir para produção

- Coloque atrás de HTTPS (Nginx, Caddy, Traefik ou o proxy da hospedagem).
- Troque as senhas padrão e use um banco PostgreSQL com backup.
- `spring.jpa.hibernate.ddl-auto=update` serve para começar; para evoluir o esquema com segurança, adote Flyway.

## Estrutura

    src/main/java/br/com/agencia/leads
      model/     Lead, LeadForm, UserAccount
      repo/      repositórios JPA
      security/  SecurityConfig, UserDetailsService, criação do admin
      web/       LeadController, UserController, LoginController
    src/main/resources/templates   telas Thymeleaf

## Colocar no ar (VPS + Docker + HTTPS automático)

1. Servidor Ubuntu com pelo menos 2 GB de RAM e um domínio/subdomínio com registro A apontando para o IP.
2. No servidor: `curl -fsSL https://get.docker.com | sh` e libere as portas 22, 80 e 443 no firewall.
3. Envie o projeto (scp ou git), entre na pasta e crie o `.env`: `cp .env.example .env` (edite todos os valores).
4. Suba tudo: `docker compose -f docker-compose.prod.yml up -d --build`
5. Acesse `https://SEU_DOMINIO` (o certificado é emitido sozinho) e troque a senha do admin em Usuários.
6. Backup: `./backup.sh`; para agendar, `crontab -e` e adicione `0 3 * * * cd /caminho/leads-agencia && ./backup.sh`
7. Atualizar a versão: envie os arquivos novos e rode o comando do passo 4 de novo (os dados ficam no volume do banco).
