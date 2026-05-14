package dev.mariscore.util;

import dev.mariscore.MarisCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

public final class Msg {
    private static final Pattern HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static MarisCorePlugin plugin;
    private static FileConfiguration messages;

    private Msg() {}

    public static void init(MarisCorePlugin p) {
        plugin = p;
        File file = new File(p.getDataFolder(), "messages.yml");
        if (!file.exists()) p.saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public static String raw(String path) {
        return messages == null ? "&cMissing messages.yml" : messages.getString(path, "&cMissing message: " + path);
    }

    public static String get(String path, Map<String, String> placeholders) {
        String out = raw(path);
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                out = out.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        return c(out);
    }

    public static void send(CommandSender sender, String path) {
        send(sender, path, Map.of());
    }

    public static void send(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(get(path, placeholders));
    }

    public static void bar(Player player, String path, Map<String, String> placeholders) {
        String msg = get(path, placeholders);
        if (msg.isBlank()) return;
        player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(msg)
        );
    }

    public static void play(Player player, String path) {
        if (messages == null || !messages.getBoolean(path + ".enabled", true)) return;
        String name = messages.getString(path + ".name", "ENTITY_EXPERIENCE_ORB_PICKUP");
        float volume = (float) messages.getDouble(path + ".volume", 1.0D);
        float pitch = (float) messages.getDouble(path + ".pitch", 1.0D);
        try {
            player.playSound(player.getLocation(), Sound.valueOf(name.toUpperCase()), volume, pitch);
        } catch (IllegalArgumentException ignored) {
            if (plugin != null) plugin.getLogger().warning("Invalid sound in messages.yml at " + path + ".name: " + name);
        }
    }

    public static String c(String s) {
        var m = HEX.matcher(s == null ? "" : s);
        var b = new StringBuffer();
        while (m.find()) {
            String h = m.group(1);
            m.appendReplacement(b, "§x§" + h.charAt(0) + "§" + h.charAt(1) + "§" + h.charAt(2) + "§" + h.charAt(3) + "§" + h.charAt(4) + "§" + h.charAt(5));
        }
        m.appendTail(b);
        return ChatColor.translateAlternateColorCodes('&', b.toString());
    }
}
