/*
 * This file is part of MapReflectionAPI.
 * Copyright (c) 2023 inventivetalent / SBDevelopment - All Rights Reserved
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

package tech.sbdevelopment.mapreflectionapi.api.events.types;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.event.HandlerList;

@NoArgsConstructor
public class Event extends org.bukkit.event.Event {
    public Event(boolean isAsync) {
        super(isAsync);
    }

    /**
     * A list of EventHandlers listening to this event.
     */
    @Getter
    private static final HandlerList handlerList = new HandlerList();

    /**
     * Get the EventHandlers listening to this event.
     *
     * @return The EventHandlers listening to this event.
     */
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
