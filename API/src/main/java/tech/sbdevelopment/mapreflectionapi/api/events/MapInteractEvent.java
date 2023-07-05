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
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.api.MapWrapper;

/**
 * This event gets fired when a player interact with a map
 */
@RequiredArgsConstructor
@Getter
public class MapInteractEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    @Setter
    private boolean cancelled;

    private final Player player;
    private final int entityID;
    private final int action;
    private final Vector vector;
    private final int hand;
    private ItemFrame frame;
    private MapWrapper mapWrapper;

    /**
     * Construct a new {@link MapInteractEvent}
     *
     * @param player   The player who interacted
     * @param entityID The ID of the entity the map is in
     * @param action   The interact action
     * @param vector   The location of the entity
     * @param hand     The hand the player clicked with
     * @param isAsync  Is this event called async?
     */
    public MapInteractEvent(Player player, int entityID, int action, Vector vector, int hand, boolean isAsync) {
        super(isAsync);
        this.player = player;
        this.entityID = entityID;
        this.action = action;
        this.vector = vector;
        this.hand = hand;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    /**
     * Get the {@link ItemFrame} the map is in
     *
     * @return The frame the map is in, or null if it's not a map
     */
    @Nullable
    public ItemFrame getFrame() {
        if (getMapWrapper() == null) return null;

        if (frame == null) {
            frame = getMapWrapper().getController().getItemFrameById(player.getWorld(), entityID);
        }
        return frame;
    }

    /**
     * Get the {@link MapWrapper} of the map
     *
     * @return The wrapper
     */
    @Nullable
    public MapWrapper getMapWrapper() {
        if (mapWrapper == null) {
            mapWrapper = MapReflectionAPI.getMapManager().getWrapperForId(player, entityID);
        }

        return mapWrapper;
    }
}
