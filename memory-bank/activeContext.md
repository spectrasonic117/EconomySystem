# Active Context

This file tracks the project's current status, including recent changes, current goals, and open questions.
2026-04-21 20:47:56 - Log of updates made.

*

## Current Focus

* Stabilise database connection handling and ensure proper shutdown.
* Add permission checks for admin commands.
* Refactor message formatting to use `MessageUtils` consistently.

## Recent Changes

* Implemented plugin enable/disable lifecycle in `Main`.
* Added database abstraction and SQLite default.
* Integrated Vault and PlaceholderAPI hooks.

## Open Questions/Issues

* Should we support MySQL/MariaDB configuration via config.yml?
* How to handle concurrency on balance updates?
* Need unit tests for command execution logic.
