---
name: ops-hub-workflow
description: "Use for Personal Ops Hub tasks that need the full project lifecycle: Obsidian intake, scoped implementation, validation, documentation, commit, and push."
---

# Ops Hub Workflow

## Procedure

1. Read `.codex/harnesses/personal-ops-hub/HARNESS.md` and `orchestrator.md`.
2. Read the user-supplied Obsidian project memory folder:
   - `index.md`
   - `hot.md`
   - `session-state.md`
   - `todo.md`
   - Current monthly session log.
3. Check code repo and Obsidian repo status. Preserve unrelated dirty files.
4. Add a `todo.md` item under `진행 중` for any implementation/documentation task.
5. Implement the smallest coherent change that fully satisfies the request.
6. Run targeted validation from `ops-hub-validation`.
7. Update Obsidian memory after validation.
8. Commit code and docs separately when appropriate. Push when requested or when continuing the established workflow.
9. Final response in Korean: changed behavior, validation, commit/push, and residual risk.

## Project Conventions

- Prefer existing Kotlin/Ktor patterns, service classes, DTOs, and `ServerTest.kt` test style.
- Keep dashboard assets static and dependency-light.
- Keep dashboard text bilingual when user-visible copy changes.
- Keep all personal/server-specific values in environment variables or docs placeholders.
- Use Obsidian docs as project memory, not as a replacement for source-of-truth code or README details.
