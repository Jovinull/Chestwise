# 0001: Public project name

- Status: accepted
- Date: 2026-08-25
- Public name: Chestwise
- Permanent mod id: `chestwise`
- Permanent Maven group and Java package root: `dev.chestwise`

## Context

`STOCKPILE` is a codename and collides with an older Minecraft mod. The initial
suggestion, StoreGrid, had no exact Minecraft project in the Modrinth API on the
date checked, but it already named Vembu backup software, a commercial
WooCommerce theme, and several GitHub repositories. That makes search results
ambiguous and creates avoidable brand risk.

## Decision

Use **Chestwise**. It is short, pronounceable, storage-related, and implies a
small quality-of-life layer rather than a technological storage network.

Checks performed on 2026-08-25:

- Modrinth search API: zero results and no exact `chestwise` slug.
- CurseForge web/search-engine search: no project named Chestwise.
- GitHub repository search API: zero repositories with the exact name.
- General web search: no relevant software or game product using the name.

The checks establish reasonable collision confidence; they are not a trademark
opinion or a reservation of platform slugs.

## Consequences

Published artifacts use `chestwise-<loader>-<game>-<version>.jar`. Existing
world identifiers remain stable even if marketing copy changes later.
