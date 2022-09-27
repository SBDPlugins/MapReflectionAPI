/*
 * This file is part of MapReflectionAPI.
 * Copyright (c) 2022 inventivetalent / SBDevelopment - All Rights Reserved
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
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import tech.sbdevelopment.mapreflectionapi.api.ArrayImage;
import tech.sbdevelopment.mapreflectionapi.api.MapWrapper;

/**
 * This event gets fired when the content of a {@link MapWrapper} is updated
 */
@RequiredArgsConstructor
@Getter
public class MapContentUpdateEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    private final MapWrapper wrapper;
    private final ArrayImage content;
    @Setter
    private boolean sendContent = true;

    /**
     * Construct a new {@link MapContentUpdateEvent}
     *
     * @param wrapper The wrapper that will be updated
     * @param content The content that will be shown
     * @param isAsync Is this event called async?
     */
    public MapContentUpdateEvent(MapWrapper wrapper, ArrayImage content, boolean isAsync) {
        super(isAsync);
        this.wrapper = wrapper;
        this.content = content;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}
