/*
 * This file is part of MapReflectionAPI.
 * Copyright (c) 2022 inventivetalent / SBDevelopment - All Rights Reserved
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package tech.sbdevelopment.mapreflectionapi;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import tech.sbdevelopment.mapreflectionapi.api.MapManager;
import tech.sbdevelopment.mapreflectionapi.cmd.MapManagerCMD;
import tech.sbdevelopment.mapreflectionapi.listeners.MapListener;
import tech.sbdevelopment.mapreflectionapi.listeners.PacketListener;
import tech.sbdevelopment.mapreflectionapi.managers.Configuration;
import tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtil;

import java.util.logging.Level;

public class MapReflectionAPI extends JavaPlugin {
    private static MapReflectionAPI instance;
    private static MapManager mapManager;

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
        getLogger().info("MapReflectionAPI v" + getDescription().getVersion() + "");
        getLogger().info("Made by © Copyright SBDevelopment 2022");

        if (!ReflectionUtil.supports(12)) {
            getLogger().severe("MapReflectionAPI only supports Minecraft 1.12.x - 1.19.x!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("BKCommonLib")) {
            getLogger().severe("MapReflectionAPI requires BKCommonLib to function!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            getLogger().severe("MapReflectionAPI requires ProtocolLib to function!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Loading the configuration...");
        Configuration.init(this);

        getLogger().info("Loading the commands...");
        getCommand("mapmanager").setExecutor(new MapManagerCMD());

        getLogger().info("Loading the map manager...");
        mapManager = new MapManager();

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
            getLogger().info("Found " + occupiedIDs + " occupied Map IDs." + (occupiedIDs > 0 ? " These will not be used." : ""));
        }

        getLogger().info("Registering the listeners...");
        Bukkit.getPluginManager().registerEvents(new MapListener(), this);
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this));

        getLogger().info("MapReflectionAPI is enabled!");
        getLogger().info("----------------");
    }

    @Override
    public void onDisable() {
        getLogger().info("MapReflectionAPI is disabled!");

        instance = null;
    }
}
