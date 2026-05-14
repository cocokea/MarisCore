# MarisCore

MarisCore is the shared economy core for the Maris plugin stack. It provides Vault-backed money, a custom shards currency, player account storage, and payment integration hooks for other Maris plugins.

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
- PlaceholderAPI is optional
- MarisSettings is optional

## Installation

1. Put the plugin jar in `plugins`.
2. Install `Vault`.
3. Install an economy frontend only if your server uses one.
4. Start the server once.
5. Edit `config.yml` and `messages.yml`.
6. Restart the server.

If Vault is missing, MarisCore disables itself during startup.

## Quick Setup

For a simple local setup:

```yml
storage:
  type: sqlite
settings:
  starting-money: 0
  starting-shards: 0
```

For MySQL:

```yml
storage:
  type: mysql
  mysql:
    host: localhost
    port: 3306
    database: mariscore
    username: root
    password: ''
    ssl: false
```

## Storage Setup

### SQLite

Default database file:

- `plugins/MarisCore/mariscore.db`

Use SQLite for a single server where setup simplicity matters more than shared access.

### MySQL

Set these values in `config.yml`:

- `storage.mysql.host`
- `storage.mysql.port`
- `storage.mysql.database`
- `storage.mysql.username`
- `storage.mysql.password`
- `storage.mysql.ssl`

Pool controls:

- `storage.pool.maximumPoolSize`
- `storage.pool.minimumIdle`

Use MySQL if multiple services or external tooling need access to account data.

## Commands

### Player Commands

- `/bal` - Check your balance.
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

## Command Examples

```text
/bal
/pay maris7 50000
/eco give maris7 100000
/shards set maris7 250
```

## Permissions

- `mariscore.admin.eco` - Access `/eco` admin actions.
- `mariscore.admin.shards` - Access shard admin actions.

## PlaceholderAPI

If PlaceholderAPI is installed, MarisCore registers:

- `%shards_value%`
- `%shards_value_formatted%`

## MarisSettings Integration

If `MarisSettings` is present, MarisCore respects:

- `PAY_TOGGLE`
- `PAY_ALERTS`

That means a player can block incoming payments or disable payment alerts through the settings GUI.

## Files

- `config.yml` - Storage backend, pool settings, and starting balances.
- `messages.yml` - Chat, actionbar, and sound message configuration.
- `plugin.yml` - Plugin metadata, commands, and runtime libraries.

## Common Mistakes

- Missing `Vault` causes startup failure.
- Switching from SQLite to MySQL without migrating data makes balances appear reset.
- Setting a very small MySQL pool on a busy server causes unnecessary queueing.

## Notes

- This plugin is marked as Folia supported.
- Account data is cached after load.
- MarisCore is intended to be a dependency layer for other Maris plugins, not just a standalone economy jar.