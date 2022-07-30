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

    private Configuration(JavaPlugin plugin) {
        this.file = new YamlFile(plugin, "config");
        this.file.loadDefaults();
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
    }
}
