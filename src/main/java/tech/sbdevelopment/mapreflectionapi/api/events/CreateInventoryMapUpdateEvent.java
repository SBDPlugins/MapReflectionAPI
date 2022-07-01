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
    public HandlerList getHandlers() {
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
