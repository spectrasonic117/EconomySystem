# System Patterns *Optional*

This file documents recurring patterns and standards used in the project.
It is optional, but recommended to be updated as the project evolves.
2026-04-21 20:47:56 - Log of updates made.

*

## Coding Patterns

* Use Lombok (`@Getter`, `@Setter`, `@RequiredArgsConstructor`) for data classes and managers.
* Centralised message formatting via `MessageUtils`.
* Database operations encapsulated in `DatabaseManager` with async handling via Bukkit scheduler.
* Commands defined as separate classes, each handling its own permission and argument validation.

## Architectural Patterns

*   

## Testing Patterns

*   