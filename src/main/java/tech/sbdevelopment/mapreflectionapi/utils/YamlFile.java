/*
 * This file is part of MapReflectionAPI.
 * Copyright (c) 2022 inventivetalent / SBDevelopment - All Rights Reserved
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

package tech.sbdevelopment.mapreflectionapi.utils;

import com.google.common.io.ByteStreams;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class YamlFile {
    private final JavaPlugin plugin;
    private final String name;
    private FileConfiguration fileConfiguration;
    private File file;

    public YamlFile(JavaPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;

        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdir()) {
            plugin.getLogger().severe("Couldn't generate the pluginfolder!");
            return;
        }

        this.file = new File(plugin.getDataFolder(), name + ".yml");
        if (!this.file.exists()) {
            try {
                if (!this.file.createNewFile()) {
                    plugin.getLogger().severe("Couldn't generate the " + name + ".yml!");
                    return;
                }
                plugin.getLogger().info("Generating the " + name + ".yml...");
            } catch (IOException e) {
                plugin.getLogger().severe("Couldn't generate the " + name + ".yml!");
                return;
            }
        }
        this.fileConfiguration = YamlConfiguration.loadConfiguration(this.file);
    }

    public void loadDefaults() {
        try {
            InputStream in = plugin.getResource(name + ".yml");
            if (in == null) {
                plugin.getLogger().severe("Expected the resource " + name + ".yml, but it was not found in the plugin JAR!");
                return;
            }

            OutputStream out = new FileOutputStream(this.file);
            ByteStreams.copy(in, out);
            reload();
        } catch (IOException e) {
            plugin.getLogger().severe("Couldn't load the default " + name + ".yml!");
        }
    }

    public FileConfiguration getFile() {
        return this.fileConfiguration;
    }

    public void save() {
        try {
            this.fileConfiguration.save(this.file);
        } catch (IOException e) {
            plugin.getLogger().severe("Couldn't save the " + name + ".yml!");
        }
    }

    public void reload() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(this.file);
    }
}