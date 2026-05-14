# MarisCore

MarisCore is a Folia-safe economy core plugin that provides Vault-backed money, a custom shards currency, and optional MarisSettings integration for payment toggles.

## What It Handles

- Vault economy provider registration
- Player money storage
- Player shards storage
- `/bal`, `/eco`, `/pay`, and `/shards` command flow
- Optional PlaceholderAPI shards placeholders
- Optional MarisSettings checks for payment toggles and payment alerts
- SQLite or MySQL storage through HikariCP

## Requirements

- Paper / Folia 1.21+
- Java 21+
- Vault
- A chat or GUI economy frontend is optional
- PlaceholderAPI is optional
- MarisSettings is optional

## Installation

1. Put the plugin jar in `plugins`.
2. Install `Vault`.
3. Start the server once.
4. Edit `config.yml` and `messages.yml`.
5. Restart the server.

## Storage Setup

MarisCore supports:

- SQLite
- MySQL

Select the backend in `config.yml`:

```yml
storage:
  type: sqlite
```

### SQLite

Default SQLite file:

- `plugins/MarisCore/mariscore.db`

Use SQLite if you want a simple single-server setup.

### MySQL

Fill these values in `config.yml`:

- `storage.mysql.host`
- `storage.mysql.port`
- `storage.mysql.database`
- `storage.mysql.username`
- `storage.mysql.password`
- `storage.mysql.ssl`

Pool sizing is controlled here:

- `storage.pool.maximumPoolSize`
- `storage.pool.minimumIdle`

## Commands

### Player Commands

- `/bal` - Check your money balance.
- `/pay <player> <amount>` - Send money to another player.
- `/shards` - Check your shard balance.

### Admin Commands

- `/eco give <player> <amount>`
- `/eco take <player> <amount>`
- `/eco set <player> <amount>`
- `/eco reset <player>`
- `/shards give <player> <amount>`
- `/shards take <player> <amount>`
- `/shards set <player> <amount>`
- `/shards reset <player>`

## Permissions

- `mariscore.admin.eco` - Access `/eco` admin actions.
- `mariscore.admin.shards` - Access shard admin actions.

## PlaceholderAPI

If PlaceholderAPI is installed, MarisCore registers shards placeholders.

Available placeholders:

- `%shards_value%`
- `%shards_value_formatted%`

## MarisSettings Integration

If `MarisSettings` is present, MarisCore can respect per-player toggles for:

- `PAY_TOGGLE`
- `PAY_ALERTS`

This means players can block incoming payments or disable payment alert messages depending on your server setup.

## Files

- `config.yml` - Storage and starting balance settings.
- `messages.yml` - Chat, actionbar, and sound message configuration.
- `plugin.yml` - Plugin metadata, commands, and runtime libraries.

## Notes

- This plugin is marked as Folia supported.
- The storage layer caches account data in memory after load.
- Vault must be available or the plugin will disable itself during startup.