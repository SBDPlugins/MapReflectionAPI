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
import org.bukkit.entity.Player;
import tech.sbdevelopment.mapreflectionapi.api.events.types.CancellableEvent;

/**
 * This event gets fired when a map creation is cancelled
 */
@RequiredArgsConstructor
@Getter
public class MapCancelEvent extends CancellableEvent {
    private final Player player;
    private final int id;

    /**
     * Construct a new {@link MapCancelEvent}
     *
     * @param player  The player who tried to create the map
     * @param id      The ID of the map
     * @param isAsync Is this event called async?
     */
    public MapCancelEvent(Player player, int id, boolean isAsync) {
        super(isAsync);
        this.player = player;
        this.id = id;
    }
}
