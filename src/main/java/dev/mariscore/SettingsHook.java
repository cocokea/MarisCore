package dev.mariscore;

import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.UUID;

public final class SettingsHook {
    private final JavaPlugin plugin;
    private Class<?> apiClass;
    private Method isEnabledMethod;

    public SettingsHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled(UUID uuid, String feature, boolean defaultValue) {
        Object api = api();
        if (api == null || isEnabledMethod == null) {
            return defaultValue;
        }
        try {
            Object result = isEnabledMethod.invoke(api, uuid, feature, defaultValue);
            return result instanceof Boolean value ? value : defaultValue;
        } catch (Throwable ignored) {
            clear();
            return defaultValue;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object api() {
        try {
            if (!plugin.getServer().getPluginManager().isPluginEnabled("MarisSettings")) {
                clear();
                return null;
            }
            if (apiClass == null) {
                apiClass = Class.forName("com.maris7.settings.api.MarisSettingsApi");
            }
            Object api = plugin.getServer().getServicesManager().load((Class) apiClass);
            if (api == null) {
                return null;
            }
            if (isEnabledMethod == null) {
                isEnabledMethod = api.getClass().getMethod("isEnabled", UUID.class, String.class, boolean.class);
            }
            return api;
        } catch (Throwable ignored) {
            clear();
            return null;
        }
    }

    private void clear() {
        apiClass = null;
        isEnabledMethod = null;
    }
}