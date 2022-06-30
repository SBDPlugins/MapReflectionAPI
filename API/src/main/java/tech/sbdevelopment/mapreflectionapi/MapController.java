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

package tech.sbdevelopment.mapreflectionapi;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import tech.sbdevelopment.mapreflectionapi.exceptions.MapLimitExceededException;

public interface MapController {
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
     * Get the map ID for a player
     *
     * @param player {@link OfflinePlayer} to get the ID for
     * @return the ID, or <code>-1</code> if no ID exists (i.e. the player is not viewing)
     */
    int getMapId(OfflinePlayer player);

    /**
     * Update the image
     *
     * @param content new {@link ArrayImage} content
     */
    void update(ArrayImage content);

    ArrayImage getContent();

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
}
