---
name: ops-hub-validation
description: "Use to select and run Personal Ops Hub validation commands for Kotlin/Ktor, dashboard JavaScript/CSS/HTML, shell scripts, API smoke checks, and browser screenshots."
---

# Ops Hub Validation

## Baseline Checks

Run these for most code changes:

```bash
./gradlew test processResources
node --check src/main/resources/dashboard/dashboard.js
git diff --check
```

Run shell syntax checks when scripts or deployment docs change:

```bash
bash -n scripts/install-systemd.sh scripts/update-server.sh scripts/report-backup.sh
```

Run harness structural validation when harness files change:

```bash
python3 ~/.codex/skills/codex-harness/scripts/validate_harness.py .codex/harnesses/personal-ops-hub
```

## Targeted Checks

- Backend route/service/config changes: `./gradlew test`, plus API smoke if the route is not fully covered.
- Database or retention changes: add tests for old/new rows, protected states, and migration behavior.
- Dashboard JS changes: `node --check`, `./gradlew processResources`, and browser smoke.
- Dashboard CSS/layout changes: browser screenshot at desktop and relevant mobile viewport.
- Systemd/Docker/log/action changes: fake binary smoke tests where possible, and tests for allowlist refusal paths.
- Install/update scripts: `bash -n`, README/runbook review, and public placeholder scan.
- Docs-only changes: structural validation or link/path sanity as relevant; code tests may be skipped with a reason.

## Browser Smoke Pattern

Use the Browser plugin or Playwright for local dashboard targets. Save screenshots under `output/playwright/` with descriptive names.

Confirm the specific behavior changed, not just that the page loads. Examples:

- Theme: `dataset.theme`, `localStorage`, and selected segmented control state.
- Mobile: `scrollWidth <= clientWidth` for nav, or expected card/list display for inventory rows.
- Actions/logs: expected buttons exist only for allowed rows and refused actions show readable feedback.
- Event UX: labels, selected state, detail panel copy, and button spacing.

## Evidence Format

Record:

- Command.
- Pass/fail.
- Important output line or screenshot path.
- Skipped checks and why they were not applicable.
