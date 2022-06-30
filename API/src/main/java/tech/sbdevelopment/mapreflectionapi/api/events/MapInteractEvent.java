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

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.api.MapWrapper;

public class MapInteractEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final Player player;
    private final int entityID;
    private final int action;
    private final Vector vector;
    private final int hand;
    private ItemFrame frame;
    private MapWrapper mapWrapper;
    private boolean cancelled;

    public MapInteractEvent(Player player, int entityID, int action, Vector vector, int hand) {
        this.player = player;
        this.entityID = entityID;
        this.action = action;
        this.vector = vector;
        this.hand = hand;
    }

    public MapInteractEvent(Player player, int entityID, int action, Vector vector, int hand, boolean isAsync) {
        super(isAsync);
        this.player = player;
        this.entityID = entityID;
        this.action = action;
        this.vector = vector;
        this.hand = hand;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public Player getPlayer() {
        return player;
    }

    public int getEntityID() {
        return entityID;
    }

    public int getAction() {
        return action;
    }

    public Vector getVector() {
        return vector;
    }

    public int getHand() {
        return hand;
    }

    public ItemFrame getFrame() {
        if (frame == null) {
            frame = getMapWrapper().getController().getItemFrameById(player.getWorld(), entityID);
        }
        return frame;
    }

    public MapWrapper getMapWrapper() {
        if (mapWrapper == null) {
            mapWrapper = MapReflectionAPI.getMapManager().getWrapperForId(player, entityID);
        }

        return mapWrapper;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
