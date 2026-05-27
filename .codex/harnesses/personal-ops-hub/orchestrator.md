# Personal Ops Hub Orchestrator

## Execution Mode

Codex role-contract orchestration for Personal Ops Hub. One Codex session may execute all roles directly. If multi-agent tooling is available and the task is large, independent review roles may be run in parallel, but the final synthesis remains the responsibility of the supervisor.

## Phase Table

| Phase | Owner | Action | Output |
| --- | --- | --- | --- |
| 0. Context check | `ops-planner` | Confirm repo paths, current date, dirty worktrees, and latest user request. | Scope notes and protected user changes. |
| 1. Memory intake | `ops-planner` | Read required Obsidian files and add a `todo.md` item for this session. | Updated todo entry. |
| 2. Route work | `ops-planner` | Select the smallest role set needed for the request. | Role coverage list. |
| 3. Implement | Selected producer roles | Make scoped code/docs changes following local patterns. | Edited files. |
| 4. Safety review | `security-deploy-operator` when relevant | Check secrets, allowlists, command execution, auth, and destructive-action boundaries. | Safety notes or fixes. |
| 5. Validate | `validation-documentation-steward` | Run targeted commands from `validation/dry-run.md` and `ops-hub-validation`. | Command evidence. |
| 6. Browser smoke | `dashboard-frontend-engineer` when UI changed | Use Browser/Playwright to inspect dashboard pages and screenshots. | Screenshot paths and observations. |
| 7. Memory sync | `validation-documentation-steward` | Update Obsidian `todo`, `hot`, `session-state`, and monthly session log. | Updated project memory. |
| 8. Commit and push | `validation-documentation-steward` | Commit code and docs when requested or when continuing the established project workflow. | Commit hashes and push result. |
| 9. Handoff | Supervisor | Summarize changed files, validation, commits, and residual risk. | Final response. |

## Role Contracts

| Role | Contract | Primary output when a run artifact is needed |
| --- | --- | --- |
| `ops-planner` | `agents/ops-planner.md` | `runs/YYYYMMDD-HHMMSS/ops-planner.md` |
| `ktor-backend-engineer` | `agents/ktor-backend-engineer.md` | `runs/YYYYMMDD-HHMMSS/ktor-backend-engineer.md` |
| `dashboard-frontend-engineer` | `agents/dashboard-frontend-engineer.md` | `runs/YYYYMMDD-HHMMSS/dashboard-frontend-engineer.md` |
| `security-deploy-operator` | `agents/security-deploy-operator.md` | `runs/YYYYMMDD-HHMMSS/security-deploy-operator.md` |
| `validation-documentation-steward` | `agents/validation-documentation-steward.md` | `runs/YYYYMMDD-HHMMSS/validation-documentation-steward.md` |

## Routing Rules

- Backend/API/database changes: use `ktor-backend-engineer`, `ops-hub-safety` if commands or credentials are involved, then `validation-documentation-steward`.
- Dashboard UI changes: use `dashboard-frontend-engineer`, then browser smoke and `validation-documentation-steward`.
- Deployment/script/server-control changes: use `security-deploy-operator`, then `ktor-backend-engineer` if API code is touched.
- Documentation-only changes: use `ops-planner` and `validation-documentation-steward`.
- Ambiguous "keep improving" requests: start with `ops-planner`, inspect docs/status/tests, choose one high-value narrow improvement, then complete it end to end.

## Artifact Paths

- Harness package: `.codex/harnesses/personal-ops-hub/`
- Optional run notes: `.codex/harnesses/personal-ops-hub/runs/YYYYMMDD-HHMMSS/`
- Project memory: the external Obsidian project memory folder supplied by the user.
- Smoke screenshots: `output/playwright/`
- Temporary smoke data: `build/*`

## Retry And Fallback Policy

- Retry deterministic validation failures once after reading the error and making a targeted fix.
- Do not run destructive commands such as `git reset --hard`, `git checkout --`, service stop, Docker stop, or database deletion unless the user explicitly requested that operation or a test fixture was created for that purpose.
- If a browser smoke cannot run, keep code validated with static checks and report the missing visual coverage.
- If Obsidian has unrelated dirty files, leave them untouched and commit only project-memory files that this task changed.
- If a new decision would alter the safety model, ask the user before implementing.

## Partial Rerun Policy

For follow-up fixes, rerun only the phases affected by the changed files, then always rerun:

1. Safety review when management/deployment/log command surfaces changed.
2. Targeted validation.
3. Obsidian memory sync if the previous session state is now stale.
4. Final git status review.

## Final Response Requirements

Include:

- What changed, in practical terms.
- Validation commands and results.
- Commit/push status if commits were created.
- Any residual risk or skipped check.

Keep the final response concise and in Korean unless the user asks otherwise.
