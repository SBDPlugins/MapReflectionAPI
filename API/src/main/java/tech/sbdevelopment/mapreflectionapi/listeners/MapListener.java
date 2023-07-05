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

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.MapInitializeEvent;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.managers.Configuration;

public class MapListener implements Listener {
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        MapReflectionAPI.getMapManager().clearAllMapsFor(e.getPlayer());
    }

    @EventHandler
    public void onMapInitialize(MapInitializeEvent e) {
        if (Configuration.getInstance().isAllowVanilla()) {
            int id = e.getMap().getId();
            if (id > 0) {
                MapReflectionAPI.getInstance().getLogger().info("Detected that the Map ID " + id + " got occupied. It will now not be used.");
                MapReflectionAPI.getMapManager().registerOccupiedID(id);
            }
        }
    }
}
