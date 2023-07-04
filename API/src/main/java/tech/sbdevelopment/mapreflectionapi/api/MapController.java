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
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

/**
 * A {@link MapController} controls one {@link MapWrapper}.
 */
public interface MapController extends IMapController {
    /**
     * Get the map ID for a player
     *
     * @param player {@link OfflinePlayer} to get the ID for
     * @return the ID, or <code>-1</code> if no ID exists (i.e. the player is not viewing)
     */
    int getMapId(OfflinePlayer player);

    /**
     * Show in a player's inventory
     *
     * @param player {@link Player}
     * @param slot   slot to show the map in
     * @param force  if <code>false</code>, the map will not be shown if the player is in creative mode
     */
    void showInInventory(Player player, int slot, boolean force);

    /**
     * Show in a player's inventory
     *
     * @param player {@link Player}
     * @param slot   slot to show the map in
     */
    void showInInventory(Player player, int slot);

    /**
     * Show in a player's hand
     *
     * @param player {@link Player}
     * @param force  if <code>false</code>, the map will not be shown if the player is not holding a map, or is in creative mode
     * @see #showInFrame(Player, ItemFrame, boolean)
     */
    void showInHand(Player player, boolean force);

    /**
     * Show in a player's hand
     *
     * @param player {@link Player}
     */
    void showInHand(Player player);

    /**
     * Show in an {@link ItemFrame}
     *
     * @param player {@link Player} that will be able to see the map
     * @param frame  {@link ItemFrame} to show the map in
     */
    void showInFrame(Player player, ItemFrame frame);

    /**
     * Show in an {@link ItemFrame}
     *
     * @param player {@link Player} that will be able to see the map
     * @param frame  {@link ItemFrame} to show the map in
     * @param force  if <code>false</code>, the map will not be shown if there is not Map-Item in the ItemFrame
     */
    void showInFrame(Player player, ItemFrame frame, boolean force);

    /**
     * Show in an {@link ItemFrame}
     *
     * @param player   {@link Player} that will be able to see the map
     * @param entityId Entity-ID of the {@link ItemFrame} to show the map in
     */
    void showInFrame(Player player, int entityId);

    /**
     * Show in an {@link ItemFrame}
     *
     * @param player    {@link Player} that will be able to see the map
     * @param entityId  Entity-ID of the {@link ItemFrame} to show the map in
     * @param debugInfo {@link String} to show when a player looks at the map, or <code>null</code>
     */
    void showInFrame(Player player, int entityId, String debugInfo);

    /**
     * Clear a frame
     *
     * @param player   {@link Player} that will be able to see the cleared frame
     * @param entityId Entity-ID of the {@link ItemFrame} to clear
     */
    void clearFrame(Player player, int entityId);

    /**
     * Clear a frame
     *
     * @param player {@link Player} that will be able to see the cleared frame
     * @param frame  {@link ItemFrame} to clear
     */
    void clearFrame(Player player, ItemFrame frame);

    /**
     * Get an {@link ItemFrame} by its entity ID
     *
     * @param world    The world the {@link ItemFrame} is in
     * @param entityId Entity-ID of the {@link ItemFrame}
     * @return The found {@link ItemFrame}, or <code>null</code>
     */
    ItemFrame getItemFrameById(World world, int entityId);
}
