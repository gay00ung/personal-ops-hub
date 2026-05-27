# Trigger Matrix

## Should Trigger This Harness

| Request Pattern | Roles | Required Checks |
| --- | --- | --- |
| "personal-ops-hub 이어서 작업해줘" | `ops-planner`, relevant specialist, `validation-documentation-steward` | Read Obsidian memory, git status, targeted checks. |
| "대시보드 디자인/다크모드/모바일 고쳐줘" | `dashboard-frontend-engineer`, `validation-documentation-steward` | `node --check`, `processResources`, browser smoke. |
| "Ktor API/DB/자동화 기능 추가" | `ktor-backend-engineer`, `validation-documentation-steward` | `./gradlew test processResources`. |
| "Docker/systemd/포트/로그/서버 제어" | `security-deploy-operator`, `ktor-backend-engineer`, `dashboard-frontend-engineer` as needed | Safety review, tests, shell syntax, API/browser smoke. |
| "설치/업데이트 스크립트" | `security-deploy-operator`, `validation-documentation-steward` | `bash -n`, public placeholder scan, README/runbook updates. |
| "문서 정리/옵시디언 갱신" | `ops-planner`, `validation-documentation-steward` | Git status, path sanity, no unrelated dirty file staging. |
| "하네스 개선" | `ops-planner`, `validation-documentation-steward` | `validate_harness.py`, dry run update. |

## Should Not Trigger Full Harness

- A simple explanation question that does not require project state.
- A one-off terminal command unrelated to this repository.
- Editing unrelated Obsidian notes outside `personal-ops-hub`.
- Installing global Codex skills or plugins without explicit user request.

## False Positive Guardrails

- If the user says "나중에" or asks only for advice, do not edit files.
- If the task is about the remote Ubuntu server but no credentials or commands are possible locally, update docs/runbook only when requested and report the blocker.
- If the code repo is clean but Obsidian has unrelated dirty files, do not stage those unrelated files.
