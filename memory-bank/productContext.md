# Product Context

This file provides a high-level overview of the project and the expected product that will be created. Initially it is based upon projectBrief.md (if provided) and all other available project-related information in the working directory. This file is intended to be updated as the project evolves, and should be used to inform all other modes of the project's goals and context.
2026-04-21 20:47:56 - Log of updates made will be appended as footnotes to the end of this file.

*

## Project Goal

* Provide a full‑featured economy system for Bukkit/Spigot servers, handling player balances, payments, and leaderboards.

## Key Features

* Balance command (`/balance`) showing formatted money.
* Pay command (`/pay <player> <amount>`).
* Leaderboard (`/balancetop`).
* Admin commands for setting, adding, removing balances.
* Configurable database (SQLite or MariaDB) and currency formatting.
* Integration with Vault (economy & permissions) and PlaceholderAPI.

## Overall Architecture

* `Main` class initializes managers and registers commands.
* `ConfigManager` loads config, messages, and sets up the chosen database.
* `DatabaseManager` abstraction with `LiteSQLManager` and `MariaDBManager` implementations.
* `CommandManager` registers CommandAPI commands.
* `LoadManager` handles Vault, PlaceholderAPI, and listeners.
* Utility classes (`MessageManager`, `MessageUtils`) format and send messages.