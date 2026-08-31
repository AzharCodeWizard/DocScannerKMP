# BRIEFING — 2026-09-01T00:05:00Z

## Mission
Implement complete About & Legal UI for DocScanner KMP (Milestone 2): LegalConstants, LegalDialogs, and SettingsScreen integration.

## 🔒 My Identity
- Archetype: teamwork_preview_worker_m2
- Roles: implementer, qa, specialist
- Working directory: /Users/azhar/Documents/Projects/DocScannerKMP/.agents/teamwork_preview_worker_m2
- Original parent: 2f6552ed-f39a-4e78-98e8-3122e06d7f0f
- Milestone: M2 (About & Legal UI)

## 🔒 Key Constraints
- Genuine implementation only, no dummy/facade implementations or hardcoded shortcuts.
- LegalConstants must contain dynamic version strings, full OSS licenses list, 100% offline Privacy Policy, and Terms of Service.
- LegalDialogs must provide clean, interactive, scrollable Compose dialogs.
- SettingsScreen must display version/build badges, list items to trigger dialogs, and support email intent with diagnostics.
- `./gradlew :composeApp:allTests` and `./gradlew :androidApp:assembleDebug` must pass.

## Current Parent
- Conversation ID: 2f6552ed-f39a-4e78-98e8-3122e06d7f0f
- Updated: not yet

## Task Summary
- **What to build**: 
  1. `composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/legal/LegalConstants.kt`
  2. `composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/legal/LegalDialogs.kt`
  3. Update `composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/SettingsScreen.kt`
  4. Unit test for LegalConstants / Legal models in `composeApp/src/commonTest/kotlin/com/lufick/docscanner/ui/legal/LegalTest.kt`
- **Success criteria**: All tests pass, debug build succeeds, all dialogs & legal features fully functional.
- **Interface contracts**: PROJECT.md § Settings & Legal Contracts
- **Code layout**: `composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/legal/`

## Key Decisions Made
- Use data class `OpenSourceLicense` in `LegalConstants.kt`.
- Provide full comprehensive Privacy Policy and Terms of Service text as specified in `survey_spec_assets.md` and `PROJECT.md`.
- Implement Material 3 dialogs with custom styling consistent with `LufickEmerald` theme.
- Support email feedback should construct a clean diagnostic string and trigger `PlatformShare.shareText` or mailto action.

## Artifact Index
- `.agents/teamwork_preview_worker_m2/DISPATCH.md` — Assignment prompt
- `.agents/teamwork_preview_worker_m2/BRIEFING.md` — Agent state memory
- `.agents/teamwork_preview_worker_m2/progress.md` — Liveness & task progress
- `.agents/teamwork_preview_worker_m2/handoff.md` — Final handoff report

## Change Tracker
- **Files modified**: None yet
- **Build status**: Untested
- **Pending issues**: None

## Quality Status
- **Build/test result**: Not run yet
- **Lint status**: 0 violations
- **Tests added/modified**: Pending

## Loaded Skills
- None
