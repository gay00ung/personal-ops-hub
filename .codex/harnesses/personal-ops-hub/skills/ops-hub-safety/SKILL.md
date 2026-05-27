---
name: ops-hub-safety
description: "Use for Personal Ops Hub safety review: public-repo readiness, secret handling, auth, allowlists, fixed argv command execution, and destructive operation limits."
---

# Ops Hub Safety

## Safety Model

Personal Ops Hub can observe host state and perform limited management actions. Safety comes from explicit enablement, allowlists, fixed argv commands, and conservative defaults.

## Required Checks

1. Public repo readiness:
   - No real passwords, tokens, private URLs, personal server IPs, or machine-specific absolute secrets.
   - `.env.example` uses placeholders only.
   - README/runbooks explain env keys without exposing private values.
2. Mutating action boundaries:
   - `OPS_MANAGE_ENABLED` gates management actions.
   - systemd units are controlled only when allowlisted.
   - Docker containers are controlled only when allowlisted or explicitly wildcarded by env.
   - restart-only lists are honored.
   - self-stop remains blocked unless a future explicit design decision changes it.
3. Command execution:
   - No shell-string command construction.
   - Use fixed executable and argv arrays.
   - Validate user-supplied names against inventory and allowlists before execution.
   - Capture bounded output for audit events.
4. Read-only logs:
   - systemd logs require `OPS_ALLOWED_SYSTEMD_UNITS`.
   - Docker logs require allowed container names.
   - line counts are bounded.
5. UI actions:
   - Dangerous or mutating actions must be visibly intentional.
   - Ports are not killed directly; port data guides the user to a Docker/systemd owner.

## Stop Conditions

Stop and ask the user before:

- Adding arbitrary command execution.
- Making public internet exposure easier without auth guidance.
- Allowing delete, kill, stop-self, wipe, or broad host mutation behavior.
- Committing any discovered secret.

## Validation

For safety-sensitive changes, run tests that prove both allowed and refused paths. If local Docker/systemd is unavailable, use fake binaries or document the missing host coverage.
