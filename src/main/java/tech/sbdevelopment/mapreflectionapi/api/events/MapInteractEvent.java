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
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
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
    public HandlerList getHandlers() {
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
