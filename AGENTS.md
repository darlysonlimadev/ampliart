# Repository Guidelines

## Prerequisites
- Java: OpenJDK 21 (Temurin) — projeto compatível com Java 21.
- Node.js: v24.x (npm via Node).
- Database: PostgreSQL 17.x.
- Maven Wrapper: usar `./mvnw` (não depender de Maven global).

## Project Structure & Module Organization
- `src/main/java/com/ampliart`: Spring Boot application code (controllers, services, repositories, domain).
- `src/main/resources`: configuration and assets.
- `src/main/resources/application.properties`: app configuration.
- `src/main/resources/db/migration`: Flyway SQL migrations.
- `src/main/resources/templates`: Thymeleaf templates.
- `src/main/resources/static/css`: Tailwind input/output CSS.
- `src/test/java`: JUnit/Spring Boot tests.

## Build, Test, and Development Commands
- `./mvnw spring-boot:run`: run the app locally.
- `./mvnw test`: run the test suite.
- `./mvnw clean package`: build a runnable JAR.
- `npm run tailwind:watch`: rebuild Tailwind CSS on changes.
- `npm run tailwind:build`: one-off minified Tailwind build.

## Typical Workflow
- Backend changes: `./mvnw test`
- UI/CSS changes: `npm run tailwind:build`
- Run locally: `./mvnw spring-boot:run`

## Language & Domain Naming Rules (MANDATORY)
- **Sempre que criar algo novo**, usar **Português do Brasil (pt-BR)**:
    - Java: nomes de classes, métodos, variáveis, pacotes/domínio, DTOs, serviços, etc.
    - Banco: nomes de tabelas, colunas, constraints e migrations.
    - UI: rótulos, textos de tela, mensagens e validações.
- Evitar termos em inglês quando houver equivalente claro em pt-BR.
- Manter nomes **explícitos e centrados no domínio** (ex.: `ProdutoServico`, `CategoriaRepositorio`, `CadastroProdutoController`).
- Para banco de dados, preferir **snake_case** em pt-BR (ex.: `produto`, `categoria`, `data_criacao`, `preco_unitario`), consistente com o que já existir no schema.

## Coding Style & Naming Conventions
- Java: 4-space indentation, braces on same line, follow existing package layout (`com.ampliart`).
- Naming: classes in `PascalCase`, methods/fields in `camelCase`.
- Prefer explicit, domain-centric names in Portuguese to match existing code.
- No formatter/linter is configured; keep edits consistent with nearby files.

## Testing Guidelines
- Frameworks: Spring Boot test starters (JUnit 5).
- Location: `src/test/java` with `*Tests.java` naming.
- Run with `./mvnw test`. Add focused tests alongside new services/controllers.

## Commit & Pull Request Guidelines
- Commits: mensagens curtas, imperativas e descritivas em português (ex.: “implementar categorias…”).
- PRs: incluir resumo, como testar (`./mvnw test` e/ou passos manuais) e screenshots quando houver mudança em UI.

## Post-change Commit Suggestion (MANDATORY)
- **Após qualquer alteração** (criar/editar/remover arquivos, classes, migrations, templates, etc.),
  o agente **deve sempre**:
    1. Resumir o que mudou.
    2. Sugerir um commit com mensagem em português no formato imperativo.
    3. Incluir um comando de exemplo: `git status` / `git diff` / `git commit -m "..."`.

## Configuration & Data
- Migrations: `src/main/resources/db/migration` (Flyway). Sempre adicionar novos arquivos versionados.
- **Não commitar secrets**. Preferir variáveis de ambiente ou override local (ex.: `application-local.properties` gitignored) para credenciais.
