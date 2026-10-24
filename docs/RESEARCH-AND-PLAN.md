# THE SYSTEM — public product research and development plan

Research date: 24 October 2026. Product direction: Android first, private and offline. This is a clean implementation beside the personal app, not a publication of its database or repository history.

## Decisions

1. No account, backend, telemetry, advertisements, payments, contacts, location, cloud sync, user uploads or AI in the alpha. No INTERNET permission. No private school, career or personal financial data is copied from the original project.
2. A real name conflicts with the no-PII requirement. Use selectable fictional aliases (e.g. Nova-07), never a real-name input. Use structured quest templates, numbered timetable slots, fixed expense categories and integer amounts. No unrestricted text field, imports or chat in this strict alpha. This intentionally limits customization: general-purpose free text cannot reliably reject every real name or address.
3. Budget values and schedule patterns remain sensitive even without a name. Encrypt app-private storage with AES-GCM and an Android Keystore key; disable backup and device transfer; exclude content from notifications, logs and task-switcher screenshots. No export until an encrypted, user-controlled design is reviewed.
4. Rewards are deterministic code: completed quests once, finite daily rewards, independently measured focus time. No API to set XP, level, currency, stats or items. Offline self-reported completion is not proof of real-world work. Rooted devices, modified clients, clock changes and a device owner remain outside a strong anti-cheat guarantee. No competitive leaderboard or tradable economy.
5. Chat text is not an authority boundary. The alpha has no LLM or chat. If AI is introduced later it is read-only, has no state-write tools, receives no sensitive content, and its output is treated as untrusted. Pattern matching is a UX filter, never a complete injection defense.
6. The exact metadata error is reserved: `[ERROR: System cannot process real-world metadata.]`. Unsupported metadata fields must be rejected before persistence, with no echoed payload or content logging. The exact tampering warning is reserved: `[WARNING: System tampering detected. Penalty Quest will trigger if unauthorized commands continue.]`. Because it implies an implemented consequence, do not expose that warning in a chat until an opt-in, non-punitive recovery-quest flow exists. No automatic punishment for a detector false positive.
7. A shield has two meanings: a cosmetic crest in alpha, and optional distraction protection later. Make that distinction explicit. The alpha's focus timer does not block other apps. Future app restrictions are voluntary, permission-specific, time-limited and reversible; emergency access, Settings, phone and uninstall must remain available. Never block users from revoking permissions.

## Research and implications

- **Android target level:** official guidance says new Play submissions must target API 36 from 31 August 2026. Build against and target 36. Recheck at submission: https://developer.android.com/google/play/requirements/target-sdk
- **Private app content:** WebViewAssetLoader serves packaged HTML/JS from an HTTPS origin without internet. Disable file/content access, external navigation and mixed content: https://developer.android.com/develop/ui/views/layout/webapps/load-local-content
- **Native bridge:** unrestricted JavaScript interfaces expose native actions to frames. Use an origin-restricted WebMessage listener, require main frame, constrain request schema and action types, never load remote content: https://developer.android.com/privacy-and-security/risks/insecure-webview-native-bridges
- **Mobile security:** OWASP MASVS covers storage, crypto, platform and privacy. Review both deliberate storage and indirect leaks through backups, logs, screenshots and notifications: https://mas.owasp.org/MASVS/ and https://mas.owasp.org/MASVS/controls/MASVS-STORAGE-2/
- **Keystore:** protects key material from extraction; does not protect against every compromised process or device: https://developer.android.com/privacy-and-security/keystore
- **Notifications:** request Android 13+ permission after an explicit user action, keep planner functional when denied, use generic notification content: https://developer.android.com/develop/ui/compose/notifications/notification-permission
- **Blocking:** Google Play requires accurate accessibility declarations and prominent consent for non-accessibility tools. Productivity software cannot claim it is an accessibility tool. Do not enable services without disclosure or automate unbounded decisions: https://support.google.com/googleplay/android-developer/answer/10964491
- **iOS:** Screen Time features require Family Controls entitlement and platform-specific implementation; do not promise identical blocking from an Android/WebView build: https://developer.apple.com/documentation/screentimeapidocumentation
- **Prompt injection:** OWASP documents manipulation of model behavior and excessive agency. Code-level authorization is required; a warning string cannot secure an app: https://genai.owasp.org/llmrisk/llm01-prompt-injection/ and https://genai.owasp.org/llmrisk/llm062025-excessive-agency/

These are engineering controls, not a zero-leak guarantee or legal certification. A production privacy notice, launch-market review, store declarations and independent security review remain release gates. No paid service is required for the alpha; distribution and any future backend introduce costs that must be decided separately.

## Feature scope

| Module | Alpha implementation | Later |
|---|---|---|
| Setup | Optional modules, fictional alias, cosmetic shield, currency, monthly ceiling/category limits, reminder choice | More safe template packs, revisable consent history |
| Planner | Preset quests for a selected day, effort, completion, deletion, daily reward cap | Recurrence, subtasks, safe custom labels after requirements review |
| Timetable | Weekly day/start/end/preset type/slot, overlap checks, delete | Calendar view and permission-scoped import only if privacy policy changes |
| Budget | Manual fixed-category expenses, month totals, integer minor units, category ceilings, overspend warnings | Encrypted backup, rollover and charts; no bank linking |
| Level up | XP and gold from quest/focus completion only, derived level, daily caps | Cosmetic unlocks and opt-in recovery shield mechanics |
| Focus | Foreground-only measured timer, start/cancel/finish; no early reward | Native interruption-safe timer and optional app restriction |
| Notifications | Off by default, opt-in generic daily reminder, permission denial respected | Event reminders, quiet hours editor, reboot scheduling tests |
| Privacy | No network, encrypted native storage, delete-all with confirmation | App unlock, encrypted backup and independently audited migrations |
| Accessibility | Visible focus, readable contrast, responsive layout, reduced motion | TalkBack/device matrix and large-font audit |

## Onboarding journey

Welcome/privacy → choose modules (at least one) → fictional alias + crest → optional budget currency/monthly ceiling/category caps → reminder off or 18:00 (outside fixed 22:00–08:00 quiet hours) → review and enter dashboard. All preferences editable later. Disabling a module hides it and preserves its local data; delete-all removes all records. Notification permission is separate from saving a preference.

## Threat model and acceptance criteria

| Threat | Control | Residual risk / validation |
|---|---|---|
| Another user's data exposed | No server, accounts, shared data or embedded personal data | Shared unlocked device can view data; secure device lock required |
| Metadata enters app | No arbitrary text fields/import/chat; exact-key validation on data/actions | Can encode identity in numbers/choices; no absolute PII detection claim |
| Malicious label/XSS | Enum-only domain labels, safe text rendering, strict CSP | Compromised WebView remains platform risk |
| Reward replay | One-way completion, duplicate rejection, daily cap, derived level | Self-reporting and modified clients remain possible |
| Focus clock manipulation | Monotonic foreground elapsed time; hide/reload cancels | Modified runtime can bypass; no competitive ranking |
| Backup/log leak | Keystore encryption, no payload logs, backup/transfer exclusions | Physical forensics/compromised device and OS screenshot behavior |
| Storage failure/corruption | Atomic encrypted writes; fail closed, never silently overwrite unreadable state | Recovery is deletion in alpha; no recovery key or cloud copy |
| Native bridge abuse | Exact origin/main frame, schema/size limits, allowlisted commands | Native/UI adversarial tests required |
| Coercive focus | Visible cancel, no forced restrictions or punishment | Future blocking needs separate security review |
| Supply chain | Minimal dependencies, CI checks, read-only workflow token, no signing secrets in source | Review updates, pin actions to reviewed SHAs before production |

## Development milestones

1. **Foundation/alpha (this change):** clean repo, research/architecture, deterministic engine, modular onboarding and all six basic modules, encrypted Android shell, generic reminders, engine/UI tests and Android CI. Deliver as development source, not a production security claim.
2. **Device beta:** inspect Android CI; instrumentation on API 30/33/36; Keystore restart/backup/corruption tests; permission denial/revoke, process death, notification visibility, clock/DST, large fonts and TalkBack; app unlock and migration design. Gate: no critical/high security findings and tested erase semantics.
3. **Optional protection:** separate service threat model, permission education, allowed app choices, emergency bypass, service recovery/revocation tests, Play declaration/video review. Gate: no restriction blocks essential access or uninstall.
4. **Public release:** independent MASVS-based review, current store target/declaration check, original licensed visual assets, release signing outside git, signed AAB, privacy notice, testing track, support and incident process. Public source code is distinct from public deployment. No production rollout is authorized by merely creating this repository.
5. **Future platforms:** desktop/iOS planning after beta feedback. If sync/community is desired, design anonymous identity, tenant isolation, rate limits, retention/deletion and server-authoritative rewards before implementation. Do not bolt accounts or AI onto the offline model without a new review.

## Open product choices (non-blocking)

Keep the initial app Android-only, offline, general-purpose and free of the personal quant curriculum. Interpret shield choice as a crest until the restriction milestone. Ship no AI, ads or social features. Stronger customization, sync, monetization, age audience and recovery behavior require explicit product decisions before those milestones.
