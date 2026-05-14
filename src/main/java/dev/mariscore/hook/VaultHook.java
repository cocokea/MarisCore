package dev.mariscore.hook;

import dev.mariscore.currency.MarisEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class VaultHook {
    private final JavaPlugin plugin;
    private final MarisEconomy economy;

    public VaultHook(JavaPlugin plugin, MarisEconomy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    public boolean register() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) return false;
        plugin.getServer().getServicesManager().register(Economy.class, economy, plugin, ServicePriority.Highest);
        plugin.getLogger().info("Registered MarisCore as Vault economy provider.");
        return plugin.getServer().getServicesManager().getRegistration(Economy.class) != null;
    }

    public void unregister() {
        plugin.getServer().getServicesManager().unregister(Economy.class, economy);
    }
}
