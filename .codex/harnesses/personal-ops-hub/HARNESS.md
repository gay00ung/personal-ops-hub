# Personal Ops Hub Harness

## Purpose

This harness defines the repeatable Codex workflow for Personal Ops Hub: a Kotlin/Ktor personal server operations dashboard with safe management actions, automation status, log viewing, install/update scripts, and Obsidian-backed project memory.

Use it when a task touches this repository, its dashboard, its deployment scripts, its safety model, or its project documentation.

## Environment Assumptions

- Target repository: the repository root that contains this harness.
- Project memory: an external Obsidian project folder supplied by the user, usually named `personal-ops-hub`.
- Documentation repo root: the Obsidian vault root that contains the project memory folder.
- Runtime: Kotlin/JVM, Ktor, kotlinx serialization, SQLite, Gradle wrapper, static HTML/CSS/JS dashboard assets
- Deployment target: Ubuntu server, systemd native service first, optional Docker/Caddy support
- Public repo constraint: no personal IPs, passwords, tokens, hostnames, private URLs, or machine-specific secrets in committed files
- Safety constraint: dashboard actions use feature flags, allowlists, restart-only lists, and fixed argv command execution; no arbitrary shell execution
- Normal validation: `./gradlew test processResources`, `node --check src/main/resources/dashboard/dashboard.js`, `bash -n scripts/install-systemd.sh scripts/update-server.sh scripts/report-backup.sh`, `git diff --check`

## Architecture

Pattern: `supervisor-plus-expert-pool`

Codex acts as the supervisor in a single session unless multi-agent tooling is explicitly available. The supervisor reads project memory, routes the task to the relevant role contracts, then applies a producer-reviewer gate through validation and documentation.

## Roles

| Role | Responsibility | Use When |
| --- | --- | --- |
| `ops-planner` | Define scope from the user request and Obsidian memory, protect public-repo constraints, and choose role coverage. | Every non-trivial task. |
| `ktor-backend-engineer` | Implement Kotlin/Ktor APIs, services, config, database logic, command wrappers, and tests. | API, server, DB, automation, management, logs, auth, or websocket changes. |
| `dashboard-frontend-engineer` | Implement static dashboard UI, responsive behavior, dark/light/system themes, accessibility, and browser smoke checks. | HTML/CSS/JS, visual design, interaction, i18n, mobile, or screenshot changes. |
| `security-deploy-operator` | Maintain safe server actions, systemd/Docker/Caddy deployment scripts, env examples, auth, and allowlist behavior. | Install/update/runbook, server operation, service control, Docker, ports, logs, and public exposure changes. |
| `validation-documentation-steward` | Select validation, update README/API/Obsidian memory, prepare commits, and write the final handoff. | Every task that changes files. |

## Skills

| Skill | Use |
| --- | --- |
| `ops-hub-workflow` | The project lifecycle: Obsidian intake, implementation, validation, documentation, commit, and push. |
| `ops-hub-validation` | Concrete checks for Gradle, dashboard JS, shell scripts, fake command smoke tests, API probes, and browser screenshots. |
| `ops-hub-safety` | Public-repo readiness, secret handling, allowlist design, fixed argv execution, and destructive operation limits. |

## Triggers

Run this harness for requests like:

- "personal-ops-hub 이어서 작업해줘"
- "기능 추가해줘" or "개선 여지 작업해줘" in this repository
- "서버에서 켜고 끄는 기능", "Docker/systemd/logs/ports 관리"
- "대시보드 디자인/다크모드/모바일 개선"
- "README, API 문서, 옵시디언 기록 갱신"
- "배포 스크립트/install/update 자동화"

Do not trigger the full harness for a one-line factual answer that does not require reading or editing this project.

## Required Project Memory Reads

Before scoped work, read these files:

- `personal-ops-hub/index.md`
- `personal-ops-hub/hot.md`
- `personal-ops-hub/session-state.md`
- `personal-ops-hub/todo.md`
- The current month under `personal-ops-hub/session-log/`

For safety/deployment work, also read:

- `personal-ops-hub/decisions/0002-safe-management-actions.md`
- `personal-ops-hub/runbooks/server-install-update.md`

## Artifact Paths

- Harness package: `.codex/harnesses/personal-ops-hub/`
- Optional per-run artifacts: `.codex/harnesses/personal-ops-hub/runs/YYYYMMDD-HHMMSS/`
- Project docs: the external Obsidian project memory folder supplied by the user.
- Browser screenshots: `output/playwright/`
- Smoke databases/logs: `build/*`

## Standard Completion Bar

A task is not complete until:

1. Relevant code/docs are implemented.
2. Targeted validation has passed or any skipped check is explicitly justified.
3. Obsidian `todo.md`, `session-state.md`, `hot.md`, and `session-log/YYYY-MM.md` are updated when the change affects project direction or state.
4. Public-repo safety has been checked for secrets and hardcoded personal values.
5. Git status is understood for both code repo and Obsidian repo.
6. Commits are made and pushed when the user requested ongoing committed work or explicitly asks for it.

## Change Log

| Date | Change | Target | Reason |
| --- | --- | --- | --- |
| 2026-05-27 | Created project-specific Codex harness | all | Preserve the Personal Ops Hub workflow across future sessions |
