package dev.mariscore.command;

import dev.mariscore.MarisCorePlugin;
import dev.mariscore.currency.MarisEconomy;
import dev.mariscore.storage.Storage;
import dev.mariscore.util.Money;
import dev.mariscore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Map;

public final class EcoCommand implements CommandExecutor {
    private final Storage storage;

    public EcoCommand(MarisCorePlugin plugin, MarisEconomy economy, Storage storage) {
        this.storage = storage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String sub = args.length > 0 ? args[0].toLowerCase() : "";
        if (args.length < 2 || (!sub.equals("reset") && args.length < 3)) {
            Msg.send(sender, "usage.eco");
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
                Map<String, String> ph = Map.of("player", account.name(), "money", Money.fmt(amount));

                switch (sub) {
                    case "give" -> storage.add(account.uuid(), "money", amount).thenRun(() -> Msg.send(sender, "eco.give", ph));
                    case "take" -> storage.add(account.uuid(), "money", amount.negate()).thenRun(() -> Msg.send(sender, "eco.take", ph));
                    case "set" -> storage.set(account.uuid(), "money", amount).thenRun(() -> Msg.send(sender, "eco.set", ph));
                    case "reset" -> storage.set(account.uuid(), "money", BigDecimal.ZERO).thenRun(() -> Msg.send(sender, "eco.reset", ph));
                    default -> Msg.send(sender, "common.unknown-subcommand");
                }
            } catch (Exception ex) {
                Msg.send(sender, "common.invalid-amount");
            }
        });
        return true;
    }
}
