# PhoneClaw AI — Project Instructions

## CRITICAL: Backend Fidelity Rule

**DO NOT rewrite, refactor, or modify backend logic.** The backend (data layer, model download, repositories, runtime, database, WorkManager, LiteRT integration) must remain exactly as in the original Google AI Edge Gallery app.

The only permitted changes are:
1. **Package name**: `com.openclaw.ai` → `com.phoneclaw.ai` (already done)
2. **Branding/UI**: App name, strings, colors, and new custom screens
3. **New UI screens**: Chat screen, onboarding, model picker — these are our custom additions layered on top of the original backend

If a backend file is broken, restore it to match the original Google AI Edge Gallery source — do not invent a new implementation.

## Architecture

- **Backend** (keep identical to Edge Gallery): `data/`, `di/`, `runtime/`, `worker/`, `common/` utilities
- **UI** (our additions/modifications): `ui/`, `customtasks/`
- **Allowlist JSON**: `assets/model_allowlist.json` — always keep in sync with the latest `assets/model_allowlists/` version

## Package

`com.phoneclaw.ai` — do not change this.
