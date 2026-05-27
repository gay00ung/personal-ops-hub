# Dry Run

Use this to prove the harness can guide a future session.

## Scenario A: Dashboard Copy Or Spacing Fix

1. Read project memory and add a `todo.md` item.
2. Route to `dashboard-frontend-engineer`.
3. Edit the smallest relevant CSS/JS/HTML area.
4. Run:

```bash
node --check src/main/resources/dashboard/dashboard.js
./gradlew processResources
git diff --check
```

5. Use Browser or Playwright to inspect the affected dashboard section.
6. Update Obsidian todo/session log/session state.
7. Commit code and docs if requested.

## Scenario B: Safe Management API Change

1. Read `decisions/0002-safe-management-actions.md`.
2. Route to `security-deploy-operator` and `ktor-backend-engineer`.
3. Confirm the action is allowlisted, fixed argv, and not arbitrary shell execution.
4. Add tests for allowed and refused paths.
5. Run:

```bash
./gradlew test processResources
bash -n scripts/install-systemd.sh scripts/update-server.sh scripts/report-backup.sh
git diff --check
```

6. Update `.env.example`, README, and runbooks if env behavior changed.

## Scenario C: Harness Edit

1. Edit files under `.codex/harnesses/personal-ops-hub/`.
2. Run:

```bash
python3 ~/.codex/skills/codex-harness/scripts/validate_harness.py .codex/harnesses/personal-ops-hub
git diff --check
```

3. Confirm every agent has these sections:
   - `## Role`
   - `## Inputs`
   - `## Outputs`
   - `## Workflow`
   - `## Collaboration`
   - `## Failure Behavior`
4. Confirm every skill has `name` and `description` frontmatter.
5. Update Obsidian project memory and mark the todo item complete only after validation passes.

## Structural Validation Result

Last expected command:

```bash
python3 ~/.codex/skills/codex-harness/scripts/validate_harness.py .codex/harnesses/personal-ops-hub
```

Expected result:

```text
OK: .codex/harnesses/personal-ops-hub
```
