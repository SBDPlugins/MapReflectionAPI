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