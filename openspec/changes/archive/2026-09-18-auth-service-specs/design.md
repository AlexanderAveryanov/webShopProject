# Design

## Context

The auth-service is fully implemented; OpenSpec specs directory is empty. This change introduces ground-truth specs for existing behavior so that future changes are grounded in documented contracts.

## Goals / Non-Goals

**Goals:**
- Produce OpenSpec delta specs for `user-auth` and `user-management` capabilities based on existing implementation.
- Sync these delta specs to the main `openspec/specs/` directory without archiving the change.

**Non-Goals:**
- No implementation changes; no new code is written or modified.
- No separate design decisions; the specs describe existing behavior.

## Decisions

No architectural or implementation decisions are made; this change is documentation-only. The sync operation uses the `openspec sync` mechanism to promote delta specs to main specs.

## Risks / Trade-offs

[Risk] Delta specs may drift from actual behavior if any unseen modification exists — mitigated by careful review of the produced files against the current codebase.