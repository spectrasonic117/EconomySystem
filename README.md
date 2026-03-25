# 💰 EconomySystem

**EconomySystem** is a lightweight, no-dependency economy plugin designed for Minecraft servers that need a simple and fast digital currency system. Whether you're running a Survival, CityBuild, or SkyBlock server, this plugin gives you essential economy features without unnecessary bloat.

---

## ✨ Features

- 💵 Simple digital currency system (no items or banknotes)
- ⚙️ Fully configurable currency format, decimal places, and starting balance
- 🧾 Transaction logging (payments, admin changes, etc.)
- 💬 Basic player commands: `/pay`, `/balance`, `/balancetop`
- 🛠️ Admin commands to set, add, or remove player balance
- 📦 Local database support using **LiteSQL** (`.db` file)
- 🐬 Optional support for **MariaDB** if preferred
- 🛠 No dependencies – works standalone
- 🔌 Optional **Vault** integration for full economy API compatibility
- 📊 Optional **PlaceholderAPI** support with 15+ placeholders
- 💬 Fully customizable messages via `messages.yml`
- 🌐 Designed for performance and cross-version compatibility

---

## 🧑‍💼 Commands & Permissions

| Command | Description | Permission |
|--------|-------------|------------|
| `/balance [player]` *(aliases: `/bal`, `/money`, `/coins`, `/geld`, `/pfund`)* | View your or another player's balance | `economysystem.command.balance` |
| `/pay <player> <amount>` | Send money to another player | `economysystem.command.pay` |
| `/balancetop` *(aliases: `/baltop`, `/topbalance`, `/topbal`, `/topgeld`)* | Show top 10 richest players | `economysystem.command.balancetop` |
| `/economyadmin <set/add/remove> <player> <amount>` *(alias: `/ecoa`)* | Admin command to modify balances | `economysystem.command.economyadmin` |

**Tab‑completion**: the command now supports autocompletion for sub‑commands, player names and amount examples, making admin usage faster and error‑free.

---

## 🧩 Dependencies

EconomySystem works standalone with **no required dependencies**.

However, it offers optional support for:

| Plugin | Type | Description |
|--------|------|-------------|
| [Vault](https://www.spigotmc.org/resources/vault.34315/) | Optional | Enables economy API compatibility with other plugins |
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | Optional | Provides placeholders for scoreboards, holograms, etc. |

---

## 📊 PlaceholderAPI Placeholders

If PlaceholderAPI is installed, the following placeholders are available:

### 💰 Player Balance
| Placeholder | Description |
|------------|-------------|
| `%economysystem_balance%` | Player's balance (config format) |
| `%economysystem_balance_raw%` | Player's balance (no formatting) |
| `%economysystem_balance_int%` | Player's balance (integer only) |
| `%economysystem_balance_decimals%` | Player's balance (2 decimals) |
| `%economysystem_balance_symbol%` | Player's balance with currency symbol |

### 👤 Other Player Balance
| Placeholder | Description |
|------------|-------------|
| `%economysystem_balance_other_{name}%` | Another player's balance |
| `%economysystem_balance_other_{name}_raw%` | Another player's balance (raw) |
| `%economysystem_balance_other_{name}_int%` | Another player's balance (integer) |
| `%economysystem_balance_other_{name}_decimals%` | Another player's balance (2 decimals) |
| `%economysystem_balance_other_{name}_symbol%` | Another player's balance with symbol |

### 🏆 Leaderboard
| Placeholder | Description |
|------------|-------------|
| `%economysystem_leaderboard_{pos}_name%` | Player name at position {pos} (1-10) |
| `%economysystem_leaderboard_{pos}_value%` | Balance at position {pos} (config format) |
| `%economysystem_leaderboard_{pos}_value_raw%` | Balance at position {pos} (raw) |
| `%economysystem_leaderboard_{pos}_value_int%` | Balance at position {pos} (integer) |
| `%economysystem_leaderboard_{pos}_value_symbol%` | Balance at position {pos} with symbol |

### ℹ️ Currency Info
| Placeholder | Description |
|------------|-------------|
| `%economysystem_currency_symbol%` | Currency symbol (e.g., `$`) |
| `%economysystem_currency_name%` | Currency name (e.g., `Dollar`) |

---

## ⚙️ Configuration

The plugin provides a clean and powerful configuration system. Example:

```yaml
economy:
    # The currency symbol
    currency-symbol: "$"
    # The currency name
    currency-name: "Dollar"
    # Amount of money players start with
    start-balance: 100.0
    # Decimal places for balance display (0 = integers only, 2 = two decimals)
    decimal-places: 0

database:
    type: litesql # Options: litesql, mariadb
    mariadb:
        host: "localhost"
        port: 3306
        database: "economy"
        username: "user"
        password: ""
    litesql:
        file: "economy.db"
```

✅ By default, the plugin stores all player data in a local `.db` file using LiteSQL.

🔁 You can optionally configure MariaDB for better performance and scalability.

---

## 💬 Custom Messages

All plugin messages are fully customizable in `messages.yml`:

```yaml
messages:
  prefix: "<green>[EconomySystem]</green>"
  plugin-enabled: "<green>The plugin has been enabled!</green>"
  plugin-disabled: "<red>The plugin has been disabled!</red>"
  balance-message-own: "<green>Your balance is: %balance%"
  balance-message-other: "<green>%player% balance is: %balance%"
  pay-success-sender: "<green>You have paid %player% %amount%!"
  pay-success-receiver: "<green>%player% has paid you %amount%!"
  top-list-title: "<green>Top 10 richest players:"
  # ... and more!
```

> Supports [MiniMessage format](https://docs.advntr.dev/minimessage/format.html) for colors and formatting.

---

## 📦 Compatibility

EconomySystem is designed for broad compatibility and long-term stability across various Minecraft server types and versions.

### ✅ Supported Minecraft Versions
- **1.8.x** to **1.21.x+**
- Actively tested on latest Paper/Purpur builds

### ✅ Supported Server Software
- **Paper**
- **Spigot**
- **Purpur**
- **Bukkit**
- **Any fork compatible with Bukkit API**

### 🧩 Plugin Dependencies
| Plugin | Required | Purpose |
|--------|----------|---------|
| Vault | ❌ Optional | Economy API compatibility |
| PlaceholderAPI | ❌ Optional | Placeholder support |

### 🛢️ Database Support
| Type     | Description                           | Recommended For                   |
|----------|---------------------------------------|------------------------------------|
| `litesql` | Lightweight local `.db` file (default) | Small to medium servers            |
| `mariadb` | External SQL-based database           | Larger networks or multi-server setups |

> 💡 You can switch between databases at any time via the `config.yml`.

---

### 🧪 Performance & Integration
- Minimal memory and CPU usage
- Asynchronous data handling
- Fast load times, even with thousands of player records
- Optional Vault hook for seamless economy integration
- Optional PlaceholderAPI expansion for scoreboards, holograms, and menus

---
