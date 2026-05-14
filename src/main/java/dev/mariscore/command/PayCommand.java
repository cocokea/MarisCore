package dev.mariscore.command;

import dev.mariscore.MarisCorePlugin;
import dev.mariscore.currency.MarisEconomy;
import dev.mariscore.storage.Storage;
import dev.mariscore.util.Money;
import dev.mariscore.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Map;

public final class PayCommand implements CommandExecutor {
    private final MarisCorePlugin plugin;
    private final Storage storage;

    public PayCommand(MarisCorePlugin plugin, MarisEconomy economy, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "common.players-only");
            return true;
        }
        if (args.length < 2) {
            Msg.send(sender, "usage.pay");
            return true;
        }
        if (args[0].equalsIgnoreCase(player.getName())) {
            sendSelfPay(player);
            return true;
        }

        BigDecimal amount;
        try {
            amount = Money.parse(args[1]);
            if (amount.signum() <= 0) throw new IllegalArgumentException();
        } catch (Exception ex) {
            Msg.send(player, "common.invalid-amount");
            return true;
        }

        storage.byName(args[0]).thenAccept(opt -> {
            if (opt.isEmpty()) {
                plugin.scheduler().player(player, () -> Msg.send(player, "common.player-not-found"));
                return;
            }

            var target = opt.get();
            if (target.uuid().equals(player.getUniqueId())) {
                plugin.scheduler().player(player, () -> sendSelfPay(player));
                return;
            }

            Map<String, String> senderPlaceholders = Map.of("player", target.name(), "money", Money.fmt(amount));

            if (!plugin.isPayEnabled(target.uuid())) {
                plugin.scheduler().player(player, () -> {
                    Msg.send(player, "pay.disabled.chat", senderPlaceholders);
                    Msg.bar(player, "pay.disabled.actionbar", senderPlaceholders);
                });
                return;
            }

            var balance = storage.balanceCached(player.getUniqueId(), "money");
            if (balance.compareTo(amount) < 0) {
                plugin.scheduler().player(player, () -> Msg.send(player, "pay.not-enough-money"));
                return;
            }

            storage.add(player.getUniqueId(), "money", amount.negate()).join();
            storage.add(target.uuid(), "money", amount).join();

            plugin.scheduler().player(player, () -> {
                Msg.send(player, "pay.sent.chat", senderPlaceholders);
                Msg.bar(player, "pay.sent.actionbar", senderPlaceholders);
                Msg.play(player, "pay.sent.sound");
            });

            Player onlineTarget = Bukkit.getPlayer(target.uuid());
            if (onlineTarget != null && plugin.isPayAlertsEnabled(target.uuid())) {
                Map<String, String> targetPlaceholders = Map.of("player", player.getName(), "money", Money.fmt(amount));
                plugin.scheduler().player(onlineTarget, () -> {
                    Msg.send(onlineTarget, "pay.received.chat", targetPlaceholders);
                    Msg.bar(onlineTarget, "pay.received.actionbar", targetPlaceholders);
                    Msg.play(onlineTarget, "pay.received.sound");
                });
            }
        });
        return true;
    }

    private void sendSelfPay(Player player) {
        Msg.send(player, "pay.self.chat");
        Msg.bar(player, "pay.self.actionbar", Map.of());
    }
}
