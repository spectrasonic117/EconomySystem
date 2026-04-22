# Decision Log

This file records architectural and implementation decisions using a list format.
2026-04-21 20:47:56 - Log of updates made.

*

## Decision

* Use CommandAPI for all command registration instead of Bukkit's `onCommand`.

## Rationale 

* Provides a fluent API, easier argument parsing, sub‑command handling, and built‑in permission checks.

## Implementation Details

* `CommandManager` creates a `CommandAPICommand` for each command class.
* Commands are registered in `Main.onEnable()` via `commandManager.registerCommands()`.
* All command classes extend a base `CommandBase` that defines execution logic.
