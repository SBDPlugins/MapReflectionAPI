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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class YamlFile {
    private final JavaPlugin plugin;
    private final String name;
    private FileConfiguration fileConfiguration;
    private File file;

    public YamlFile(JavaPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;

        saveDefaultFile();
    }

    public void reloadFile() {
        if (this.file == null)
            this.file = new File(this.plugin.getDataFolder(), name + ".yml");

        this.fileConfiguration = YamlConfiguration.loadConfiguration(this.file);

        InputStream defaultStream = this.plugin.getResource(name + ".yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.fileConfiguration.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getFile() {
        if (this.fileConfiguration == null)
            reloadFile();

        return this.fileConfiguration;
    }

    public void saveFile() {
        if (this.fileConfiguration == null || this.file == null)
            return;

        try {
            this.fileConfiguration.save(this.file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't save the file " + this.name + ".yml.", e);
        }
    }

    public void saveDefaultFile() {
        if (this.file == null)
            this.file = new File(this.plugin.getDataFolder(), name + ".yml");

        if (!this.file.exists())
            this.plugin.saveResource(name + ".yml", false);
    }
}