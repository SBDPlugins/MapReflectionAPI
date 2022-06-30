package tech.sbdevelopment.mapreflectionapi;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MapReflectionAPI extends JavaPlugin {
    private static MapReflectionAPI instance;
    private static MapManager mapManager;

    public static MapReflectionAPI getInstance() {
        if (instance == null) throw new IllegalStateException("The plugin is not enabled yet!");
        return instance;
    }

    public static MapManager getMapManager() {
        if (mapManager == null) throw new IllegalStateException("The plugin is not enabled yet!");
        return mapManager;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (!Bukkit.getPluginManager().isPluginEnabled("BKCommonLib")) {
            getLogger().severe("MapReflectionAPI requires BKCommonLib to function!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            mapManager = new MapManager();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("MapReflectionAPI is enabled!");
    }

    @Override
    public void onDisable() {
        instance = null;
    }
}
