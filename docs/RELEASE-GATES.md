# Release gates

The source repository is public; the product remains a development alpha. Do not advertise it as secure for public production use yet.

## Automated evidence

Record engine/security test results, Java schema tests, browser flow checks, Android lint and compilation against the final commit. Browser tests do not establish native Keystore, notification or Android lifecycle behavior. Static checks establish configuration only.

## Before beta

- API 30/33/36 device/emulator tests: cold start, relaunch, rotation, process death, screen lock, foreground/background and navigation. Verify focus earns no credit after interruptions, and early/double completion is rejected.
- Real vault tests: no plaintext task/budget data in files; write/read after process restart; ciphertext corruption/wrong key fails closed without silent reset; simulate interrupted atomic writes; confirm overwrite/erase and key deletion semantics.
- No-network verification from a clean install with all modules, including WebView. Audit merged manifest and dependency manifest contributions. Capture only synthetic test traffic/data.
- Bridge tests from rejected origins and frames, malformed/oversized schemas and payloads; no echoed metadata. Confirm no external URL/file/content navigation and no URI scheme bypass.
- Permission tests: notifications off, granted, denied, revoked; quiet hours, time zone/DST changes, delayed alarm delivery, battery saver. Reboot cancels Android alarms in alpha; launching reschedules. Add boot handling only with explicit tests.
- Delete-all confirmation: cancel preserves data, confirm removes vault/key and notification preference/pending alarms, relaunch shows fresh setup. Failure should not claim success.
- TalkBack, keyboard, 200% font scale, contrast, phone landscape, cutout/navigation insets, reduced motion and no horizontal overflow. No app blocking or wellbeing penalties.
- Current native UI does not require biometrics: device lock is the access boundary. Decide whether an app unlock is required for beta.
- Capacity: encrypted payload limited to 256 KiB; 2,000 records per collection. Show explicit errors, never silently trim records. Plan archival before broad adoption.

## Before public production

- Independent review against appropriate OWASP MASVS controls, dependency/security scan, remediation of critical/high findings and documented residual risks.
- App-specific privacy policy and data-safety declarations; market/age eligibility and compliance review; recheck target API and store policies at submission.
- Release key generated/retained outside source, tested signed AAB and update/migration behavior; pin CI actions to reviewed commit SHAs; establish branch protection and private vulnerability reporting.
- Original/licensed branding and assets. Do not copy franchise names, artwork, characters, soundtracks or protected item descriptions.
- A consent flow and policy declaration for any future accessibility/app restriction service; emergency exit, revocation, Settings access and uninstall remain possible.
- A documented incident/update/support process. No analytics, cloud or assistant SDK added without a new data-flow and threat review.

## Known limitations

Preset-only labels; no imports/exports/recovery; no real-world completion verification; no anti-root guarantee; no background focus sessions; no system-wide blocking; one approximate generic daily reminder; no per-event reminders; no reboot reminder recovery until app launch; no signed release or store publication. User-controlled time and modified clients can bypass local reward conventions. Shared or compromised devices remain a privacy risk.
