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

package tech.sbdevelopment.mapreflectionapi.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tech.sbdevelopment.mapreflectionapi.api.exceptions.MapLimitExceededException;

/**
 * This interface contains the methods for both the {@link MapController} and the {@link MultiMapController}.
 */
public interface IMapController {
    /**
     * Add a viewer
     *
     * @param player {@link Player} to add
     */
    void addViewer(Player player) throws MapLimitExceededException;

    /**
     * Remove a viewer
     *
     * @param player {@link OfflinePlayer} to remove
     */
    void removeViewer(OfflinePlayer player);

    /**
     * Remove all viewers
     */
    void clearViewers();

    /**
     * Check if a player is viewing
     *
     * @param player {@link OfflinePlayer} to check
     * @return <code>true</code> if the player is viewing
     */
    boolean isViewing(OfflinePlayer player);

    /**
     * Update the image
     *
     * @param content new {@link ArrayImage} content
     */
    void update(@NotNull ArrayImage content);

    /**
     * Send the content to a player
     *
     * @param player {@link Player} receiver of the content
     */
    void sendContent(Player player);

    /**
     * Send the content to a player
     *
     * @param player       {@link Player} receiver of the content
     * @param withoutQueue if <code>true</code>, the content will be sent immediately
     */
    void sendContent(Player player, boolean withoutQueue);

    /**
     * Cancels the 'send events' in the queue
     */
    void cancelSend();
}
