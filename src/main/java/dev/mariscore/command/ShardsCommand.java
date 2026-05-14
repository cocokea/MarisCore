package dev.mariscore.command;

import dev.mariscore.MarisCorePlugin;
import dev.mariscore.storage.Storage;
import dev.mariscore.util.Money;
import dev.mariscore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Map;

public final class ShardsCommand implements CommandExecutor {
    private final MarisCorePlugin plugin;
    private final Storage storage;

    public ShardsCommand(MarisCorePlugin plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                Msg.send(sender, "common.players-only");
                return true;
            }
            var balance = storage.balanceCached(player.getUniqueId(), "shards");
            Map<String, String> placeholders = Map.of("shards", Money.fmt(balance));
            Msg.send(player, "shards.balance.chat", placeholders);
            Msg.bar(player, "shards.balance.actionbar", placeholders);
            return true;
        }

        if (!sender.hasPermission("mariscore.admin.shards")) {
            Msg.send(sender, "common.no-permission");
            return true;
        }
        String sub = args[0].toLowerCase();
        if (args.length < 2 || (!sub.equals("reset") && args.length < 3)) {
            Msg.send(sender, "usage.shards");
            return true;
        }

        storage.byName(args[1]).thenAccept(opt -> {
            if (opt.isEmpty()) {
                Msg.send(sender, "common.player-not-found");
                return;
            }
            var account = opt.get();
            try {
                BigDecimal amount = sub.equals("reset") ? BigDecimal.ZERO : Money.parse(args[2]);
                Map<String, String> ph = Map.of("player", account.name(), "shards", Money.fmt(amount), "money", Money.fmt(amount));
                switch (sub) {
                    case "give" -> storage.add(account.uuid(), "shards", amount).thenRun(() -> Msg.send(sender, "shards.give", ph));
                    case "take" -> storage.add(account.uuid(), "shards", amount.negate()).thenRun(() -> Msg.send(sender, "shards.take", ph));
                    case "set" -> storage.set(account.uuid(), "shards", amount).thenRun(() -> Msg.send(sender, "shards.set", ph));
                    case "reset" -> storage.set(account.uuid(), "shards", BigDecimal.ZERO).thenRun(() -> Msg.send(sender, "shards.reset", ph));
                    default -> Msg.send(sender, "common.unknown-subcommand");
                }
            } catch (Exception ex) {
                Msg.send(sender, "common.invalid-amount");
            }
        });
        return true;
    }
}
