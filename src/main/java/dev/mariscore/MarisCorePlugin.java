package dev.mariscore;

import dev.mariscore.command.*;
import dev.mariscore.config.Settings;
import dev.mariscore.currency.MarisEconomy;
import dev.mariscore.hook.VaultHook;
import dev.mariscore.listener.JoinListener;
import dev.mariscore.placeholder.ShardsPlaceholders;
import dev.mariscore.scheduler.PlatformScheduler;
import dev.mariscore.storage.Storage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class MarisCorePlugin extends JavaPlugin {

    private Settings settings;
    private PlatformScheduler scheduler;
    private Storage storage;
    private MarisEconomy economy;
    private VaultHook vaultHook;
    private SettingsHook settingsHook;

    @Override public void onEnable() {
        
        saveDefaultConfig();
        MarisPluginStartup.bootstrap(this, "cocokea/MarisCore");
saveDefaultConfig();
        dev.mariscore.util.Msg.init(this);
        settings = new Settings(this);
        settingsHook = new SettingsHook(this);
        scheduler = new PlatformScheduler(this);
        storage = new Storage(this, settings);
        storage.init();
        economy = new MarisEconomy(this, storage, settings);
        vaultHook = new VaultHook(this, economy);
        if (!vaultHook.register()) { getLogger().severe("Vault not found or economy registration failed."); getServer().getPluginManager().disablePlugin(this); return; }
        getServer().getPluginManager().registerEvents(new JoinListener(this, storage, settings), this);
        getCommand("eco").setExecutor(new EcoCommand(this, economy, storage));
        getCommand("bal").setExecutor(new BalanceCommand(this, storage));
        getCommand("shards").setExecutor(new ShardsCommand(this, storage));
        getCommand("pay").setExecutor(new PayCommand(this, economy, storage));
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) new ShardsPlaceholders(this, storage).register();
    }

    @Override public void onDisable() { if (vaultHook != null) vaultHook.unregister(); if (storage != null) storage.close(); }
    public PlatformScheduler scheduler() { return scheduler; }
    public Storage storage() { return storage; }
    public boolean isPayEnabled(java.util.UUID uuid) { return settingsHook == null || settingsHook.isEnabled(uuid, "PAY_TOGGLE", true); }
    public boolean isPayAlertsEnabled(java.util.UUID uuid) { return settingsHook == null || settingsHook.isEnabled(uuid, "PAY_ALERTS", true); }

}

