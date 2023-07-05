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

package tech.sbdevelopment.mapreflectionapi.api.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.api.MapWrapper;

/**
 * This event gets fired when a map in the creative inventory gets updated
 */
@RequiredArgsConstructor
@Getter
public class CreateInventoryMapUpdateEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    @Setter
    private boolean cancelled;

    private final Player player;
    private final int slot;
    private final ItemStack item;
    private MapWrapper mapWrapper;

    /**
     * Construct a new {@link CreateInventoryMapUpdateEvent}
     *
     * @param player  The player whose inventory is updated
     * @param slot    The new slot
     * @param item    The item in the new slot
     * @param isAsync Is this event called async?
     */
    public CreateInventoryMapUpdateEvent(Player player, int slot, ItemStack item, boolean isAsync) {
        super(isAsync);
        this.player = player;
        this.slot = slot;
        this.item = item;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    /**
     * Get the {@link MapWrapper} of the map of this event
     *
     * @return The {@link MapWrapper}
     */
    @Nullable
    public MapWrapper getMapWrapper() {
        if (mapWrapper == null) {
            if (item == null) return null;
            if (item.getType() != Material.MAP) return null;
            mapWrapper = MapReflectionAPI.getMapManager().getWrapperForId(player, item.getDurability());
        }

        return mapWrapper;
    }
}
