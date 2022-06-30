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

package tech.sbdevelopment.mapreflectionapi.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import tech.sbdevelopment.mapreflectionapi.api.MapWrapper;

import java.lang.reflect.Field;

public abstract class PacketListener implements Listener {
    protected JavaPlugin plugin;

    public static PacketListener construct() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);

        try {
            final Class<?> clazz = Class.forName("tech.sbdevelopment.mapreflectionapi.nms.MapWrapper_" + version);
            if (MapWrapper.class.isAssignableFrom(clazz)) {
                return (PacketListener) clazz.getDeclaredConstructor().newInstance();
            } else {
                throw new IllegalStateException("Plugin corrupted! Detected invalid MapWrapper class.");
            }
        } catch (Exception ex) {
            throw new IllegalStateException("This Spigot version is not supported! Contact the developer to get support.");
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

    protected Object getField(Object packet, String field) throws NoSuchFieldException, IllegalAccessException {
        Field f = packet.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.get(packet);
    }

    protected void setField(Object packet, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field f = packet.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(packet, value);
    }
}
