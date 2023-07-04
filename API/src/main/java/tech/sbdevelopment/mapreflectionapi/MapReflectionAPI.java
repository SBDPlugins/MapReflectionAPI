/*
 * This file is part of MapReflectionAPI.
 * Copyright (c) 2022-2023 inventivetalent / SBDevelopment - All Rights Reserved
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.sbdevelopment.mapreflectionapi;

import com.bergerkiller.bukkit.common.map.MapColorPalette;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import tech.sbdevelopment.mapreflectionapi.api.MapManager;
import tech.sbdevelopment.mapreflectionapi.cmd.MapManagerCMD;
import tech.sbdevelopment.mapreflectionapi.listeners.MapListener;
import tech.sbdevelopment.mapreflectionapi.listeners.PacketListener;
import tech.sbdevelopment.mapreflectionapi.managers.Configuration;
import tech.sbdevelopment.mapreflectionapi.utils.MainUtil;
import tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtil;
import tech.sbdevelopment.mapreflectionapi.utils.UpdateManager;

import java.util.logging.Level;

public class MapReflectionAPI extends JavaPlugin {
    private static MapReflectionAPI instance;
    private static MapManager mapManager;
    private static PacketListener packetListener;

    /**
     * Get the plugin instance
     *
     * @return The {@link MapReflectionAPI} instance
     */
    public static MapReflectionAPI getInstance() {
        if (instance == null) throw new IllegalStateException("The plugin is not enabled yet!");
        return instance;
    }

    /**
     * Get the {@link MapManager}
     *
     * @return The manager
     */
    public static MapManager getMapManager() {
        if (mapManager == null) throw new IllegalStateException("The plugin is not enabled yet!");
        return mapManager;
    }

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("----------------");
        getLogger().info("MapReflectionAPI v" + getDescription().getVersion());
        getLogger().info("Made by © Copyright SBDevelopment 2023");

        if (!ReflectionUtil.supports(12)) {
            getLogger().severe("MapReflectionAPI only supports Minecraft 1.12 - 1.19.4!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Loading Java AWT runtime library support...");
        if (MainUtil.isHeadlessJDK()) {
            getLogger().severe("MapReflectionAPI requires the Java AWT runtime library, but is not available!");
            getLogger().severe("This is usually because a headless JVM is used for the server.");
            getLogger().severe("Please install and configure a non-headless JVM to make this plugin work.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        MapColorPalette.getColor(0, 0, 0); //Initializes the class

        getLogger().info("Loading the configuration...");
        Configuration.init(this);

        getLogger().info("Loading the commands...");
        getCommand("mapmanager").setExecutor(new MapManagerCMD());

        getLogger().info("Loading the packet listener...");
        try {
            packetListener = PacketListener.construct(this);
        } catch (IllegalStateException e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        packetListener.init(this);

        getLogger().info("Loading the map manager...");
        try {
            mapManager = new MapManager(this);
        } catch (IllegalStateException e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (Configuration.getInstance().isAllowVanilla()) {
            getLogger().info("Vanilla Maps are allowed. Discovering occupied Map IDs...");
            int occupiedIDs = 0;
            for (int s = 0; s < Short.MAX_VALUE; s++) {
                try {
                    MapView view = Bukkit.getMap(s);
                    if (view != null) {
                        mapManager.registerOccupiedID(s);
                        occupiedIDs++;
                    }
                } catch (Exception e) {
                    if (!e.getMessage().toLowerCase().contains("invalid map dimension")) {
                        getLogger().log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            getLogger().info("Found " + occupiedIDs + " occupied Map IDs" + (occupiedIDs > 0 ? ", these will not be used." : "."));
        }

        getLogger().info("Registering the listeners...");
        Bukkit.getPluginManager().registerEvents(new MapListener(), this);

        getLogger().info("Loading metrics...");
        Metrics metrics = new Metrics(this, 16033);
        metrics.addCustomChart(new SingleLineChart("managed_maps", () -> mapManager.getManagedMapsCount()));

        if (Configuration.getInstance().isUpdaterCheck()) {
            try {
                UpdateManager updateManager = new UpdateManager(this, 103011);

                updateManager.handleResponse((versionResponse, version) -> {
                    switch (versionResponse) {
                        case FOUND_NEW:
                            getLogger().warning("There is a new version available! Current: " + getDescription().getVersion() + " New: " + version.get());
                            if (Configuration.getInstance().isUpdaterDownload()) {
                                getLogger().info("Trying to download the update. This could take some time...");

                                updateManager.handleDownloadResponse((downloadResponse, fileName) -> {
                                    switch (downloadResponse) {
                                        case DONE:
                                            getLogger().info("Update downloaded! If you restart your server, it will be loaded. Filename: " + fileName);
                                            break;
                                        case ERROR:
                                            getLogger().severe("Something went wrong when trying downloading the latest version.");
                                            break;
                                        case UNAVAILABLE:
                                            getLogger().warning("Unable to download the latest version.");
                                            break;
                                    }
                                }).runUpdate();
                            }
                            break;
                        case LATEST:
                            getLogger().info("You are running the latest version [" + getDescription().getVersion() + "]!");
                            break;
                        case THIS_NEWER:
                            getLogger().info("You are running a newer version [" + getDescription().getVersion() + "]! This is probably fine.");
                            break;
                        case UNAVAILABLE:
                            getLogger().severe("Unable to perform an update check.");
                            break;
                    }
                }).check();
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }
        }

        getLogger().info("MapReflectionAPI is enabled!");
        getLogger().info("----------------");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling the packet handler...");
        if (packetListener != null) Bukkit.getOnlinePlayers().forEach(p -> packetListener.removePlayer(p));

        getLogger().info("MapReflectionAPI is disabled!");

        instance = null;
    }
}
