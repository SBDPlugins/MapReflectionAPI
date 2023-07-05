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

package tech.sbdevelopment.mapreflectionapi.cmd;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.sbdevelopment.mapreflectionapi.managers.Configuration;

import java.util.ArrayList;
import java.util.List;

public class MapManagerCMD implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("mapmanager.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have the permissions to use this command!");
            return false;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            Configuration.getInstance().reload();

            sender.sendMessage(ChatColor.GREEN + "The configuration has been reloaded!");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "Usage: " + ChatColor.WHITE + "/mapmanager reload");
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return StringUtil.copyPartialMatches(args[0], List.of("reload"), new ArrayList<>());
        return new ArrayList<>();
    }
}
