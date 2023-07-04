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

package tech.sbdevelopment.mapreflectionapi.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public abstract class PacketListener implements Listener {
    protected JavaPlugin plugin;

    public static PacketListener construct(JavaPlugin plugin) throws IllegalStateException {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);

        plugin.getLogger().info("Initializing the packet handler for Minecraft version " + version + "...");

        try {
            final Class<?> clazz = Class.forName("tech.sbdevelopment.mapreflectionapi.nms.PacketListener_" + version);
            if (PacketListener.class.isAssignableFrom(clazz)) {
                return (PacketListener) clazz.getDeclaredConstructor().newInstance();
            } else {
                throw new IllegalStateException("Plugin corrupted! Detected invalid PacketListener class.");
            }
        } catch (Exception ex) {
            throw new IllegalStateException("This Minecraft version (" + version + ") is not supported! Contact the developer to get support.");
        }
    }

    public void init(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        injectPlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        removePlayer(e.getPlayer());
    }

    protected abstract void injectPlayer(Player p);

    public abstract void removePlayer(Player p);

    protected abstract Vector vec3DToVector(Object vec3d);

    protected boolean hasField(Object packet, String field) {
        try {
            packet.getClass().getDeclaredField(field);
            return true;
        } catch (NoSuchFieldException ex) {
            return false;
        }
    }
}