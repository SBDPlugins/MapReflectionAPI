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

package tech.sbdevelopment.mapreflectionapi.managers;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import tech.sbdevelopment.mapreflectionapi.utils.YamlFile;

public class Configuration {
    private static Configuration instance;
    private final YamlFile file;

    @Getter
    private boolean allowVanilla = true;
    @Getter
    private boolean imageCache = true;
    @Getter
    private boolean updaterCheck = true;
    @Getter
    private boolean updaterDownload = true;

    private Configuration(JavaPlugin plugin) {
        this.file = new YamlFile(plugin, "config");
        reload();
    }

    public static void init(JavaPlugin plugin) {
        instance = new Configuration(plugin);
    }

    public static Configuration getInstance() {
        if (instance == null) throw new IllegalStateException("The plugin is not enabled yet!");
        return instance;
    }

    public void reload() {
        allowVanilla = this.file.getFile().getBoolean("allowVanilla");
        imageCache = this.file.getFile().getBoolean("imageCache");
        updaterCheck = this.file.getFile().getBoolean("updater.check");
        updaterDownload = this.file.getFile().getBoolean("updater.download");
    }
}
