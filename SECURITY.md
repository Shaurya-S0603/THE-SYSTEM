# Security policy

This repository is an early alpha and has no audited or supported production version yet. Do not submit personal data, vault contents, keys or exploit details in public issues. Use GitHub private vulnerability reporting if enabled; otherwise send a minimal, non-sensitive request for a private reporting channel without exploit details.

Security design and residual risks are documented in `docs/RESEARCH-AND-PLAN.md`. Public release requires the gates in `docs/RELEASE-GATES.md`. No promise of zero leaks, uncheatable offline progress, immediate memory scrubbing or forensic secure erasure is made.

There is no AI/chat interface. Text cannot issue reward commands. A future assistant must remain read-only and must not receive reward-writing or arbitrary native tools. No secret belongs in prompts, client source, workflow output or git.
