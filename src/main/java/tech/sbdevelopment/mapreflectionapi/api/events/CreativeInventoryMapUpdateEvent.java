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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.Nullable;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.api.MapWrapper;
import tech.sbdevelopment.mapreflectionapi.api.events.types.CancellableEvent;
import tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtils;
import tech.sbdevelopment.mapreflectionapi.utils.XMaterial;

/**
 * This event gets fired when a map in the creative inventory gets updated
 */
@RequiredArgsConstructor
@Getter
public class CreativeInventoryMapUpdateEvent extends CancellableEvent {
    private final Player player;
    private final int slot;
    private final ItemStack item;
    private MapWrapper mapWrapper;

    /**
     * Construct a new {@link CreativeInventoryMapUpdateEvent}
     *
     * @param player  The player whose inventory is updated
     * @param slot    The new slot
     * @param item    The item in the new slot
     * @param isAsync Is this event called async?
     */
    public CreativeInventoryMapUpdateEvent(Player player, int slot, ItemStack item, boolean isAsync) {
        super(isAsync);
        this.player = player;
        this.slot = slot;
        this.item = item;
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
            if (!XMaterial.FILLED_MAP.isSimilar(item)) return null;
            mapWrapper = MapReflectionAPI.getMapManager().getWrapperForId(player, item.getDurability());
        }

        return mapWrapper;
    }
}
