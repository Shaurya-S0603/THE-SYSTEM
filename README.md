# THE SYSTEM

A private, offline Android productivity RPG for general public use. **0.1.0 development alpha — not a production release or a security certification.**

Choose your own tools: planner, weekly timetable, manual budget, levels and focus. A clean implementation based on the personal SYSTEM app's Android/WebView approach; no personal roadmap, school data, budgets, signing key or commit history is included.

## What works in this alpha

- Guided setup: select modules, fictional alias, cosmetic crest, currency, monthly/category limits and an optional generic daily reminder. Settings can be revised later.
- Planner: dated preset quests, effort choices, complete once and remove unfinished quests.
- Weekly timetable: numbered blocks, overlap rejection and removal.
- Budget: category allocations, manual integer-cent expenses, current-month totals, overspending warnings and expense removal.
- Level up: derived levels and gold, fixed quest rewards, measured focus rewards and shared daily XP cap. No setters for level, XP or items.
- Focus: foreground-only countdown with cancel and completion validation. Leaving the app or focus screen cancels without a penalty. **No other apps are blocked.**
- Privacy settings and confirmed deletion of all local records.
- Native encrypted persistence, origin-scoped WebView messages, no network permission, no telemetry, disabled backups and screenshot protection. These controls still require device validation before public distribution.

**Strict no-PII scope:** no real-name, email, location, payment, free-text, chat or import fields. Labels are presets and numbered slots. This limits customization on purpose. No app can detect every possible identity encoded in arbitrary inputs, and an offline client cannot guarantee honesty on a modified device.

## Run checks

```sh
npm ci
npm test
npm run check
npx playwright install chromium
npm run test:ui
```

To inspect the UI without Android, serve `android/app/src/main/assets/` using a local HTTP server. Browser preview data exists in memory only and disappears on reload. It has no Android encryption or notifications. Do not deploy this preview as a public web app.

For a native build: install JDK 17, Android SDK platform/build-tools 36 and Gradle 8.11.1, then run:

```sh
cd android
gradle --no-daemon testDebugUnitTest lintDebug assembleDebug
```

CI runs engine/security assertions, browser flows at phone/tablet/desktop widths, Java schema tests, Android lint and compilation. A passing workflow produces a **development debug APK** as an Actions artifact, not a signed public release. Debug keys are generated per runner; update compatibility between alpha builds is not promised. Use a proper retained release key outside git before beta distribution. No release credentials are included.

## Architecture

- `android/app/src/main/assets/engine.js`: deterministic, transactional domain rules; schema validation and derived stats.
- `android/app/src/main/assets/app.js`: modular onboarding and accessible UI; asynchronous native storage interface.
- `android/app/src/main/java/app/thesystem/mobile/`: Android shell, allowlisted message bridge, Keystore vault, strict native schema and generic reminders.
- `tests/`: reward, privacy/schema, timetable, budget and browser integration checks.
- `docs/RESEARCH-AND-PLAN.md`: research, feature decisions, threat model, constraints, milestones and sources.
- `docs/RELEASE-GATES.md`: validation required before beta/public release.

The local vault is AES-GCM encrypted using a non-exported Android Keystore key in app-private no-backup storage. Erasing deletes the file and key; this is not a forensic secure-erase guarantee. An unlocked shared phone or compromised device can still expose app data. Data loss on uninstall/device loss is expected in this alpha; no export/recovery exists.

## Development sequence

1. Offline modular alpha and reproducible checks (this repository).
2. Device-tested beta: lifecycle, permissions, encryption/corruption, notifications, accessibility and migrations.
3. Optional, reversible app protection with separate consent and platform review.
4. Independent security review, store declarations, release signing and public testing track.
5. Evaluate iOS/desktop or anonymous sync only after feedback and a fresh threat model.

No AI, cloud sync, social profiles, bank connections, competitive economy or punitive lockouts are planned for the alpha. All reward completion is self-reported except locally measured focus time.
