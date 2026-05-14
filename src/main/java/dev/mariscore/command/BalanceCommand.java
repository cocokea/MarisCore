package dev.mariscore.command;

import dev.mariscore.MarisCorePlugin;
import dev.mariscore.storage.Storage;
import dev.mariscore.util.Money;
import dev.mariscore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public final class BalanceCommand implements CommandExecutor {
    private final MarisCorePlugin plugin;
    private final Storage storage;

    public BalanceCommand(MarisCorePlugin plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "common.players-only");
            return true;
        }

        var balance = storage.balanceCached(player.getUniqueId(), "money");
        Map<String, String> placeholders = Map.of("money", Money.fmt(balance));
        Msg.send(player, "balance.chat", placeholders);
        Msg.bar(player, "balance.actionbar", placeholders);
        return true;
    }
}
