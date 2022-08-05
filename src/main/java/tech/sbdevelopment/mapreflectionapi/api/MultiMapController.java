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

package tech.sbdevelopment.mapreflectionapi.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tech.sbdevelopment.mapreflectionapi.api.exceptions.MapLimitExceededException;

public interface MultiMapController extends IMapController {
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

    /**
     * Show this {@link MultiMapController} in {@link ItemFrame}s
     *
     * @param player         {@link Player} that will be able to see the maps
     * @param entityIdMatrix 2D-Array of entity-IDs of the {@link ItemFrame}s (<code>int[width][height]</code>)
     * @see MapController#showInFrame(Player, int)
     */
    void showInFrames(Player player, int[][] entityIdMatrix);

    /**
     * Show this {@link MultiMapController} in {@link ItemFrame}s
     *
     * @param player         {@link Player} that will be able to see the maps
     * @param entityIdMatrix 2D-Array of entity-IDs of the {@link ItemFrame}s (<code>int[width][height]</code>)
     * @param callable       {@link DebugCallable} which will be called to display debug information, or <code>null</code>
     * @see MapController#showInFrame(Player, int, String)
     */
    void showInFrames(Player player, int[][] entityIdMatrix, DebugCallable callable);

    /**
     * Show this {@link MultiMapController} in {@link ItemFrame}s
     *
     * @param player          {@link Player} that will be able to see the maps
     * @param itemFrameMatrix 2D-Array of {@link ItemFrame}s (<code>ItemFrame[width][height]</code>)
     * @param force           if <code>false</code>, the map will not be shown if there is not Map-Item in the ItemFrames
     * @see MapController#showInFrame(Player, ItemFrame, boolean)
     */
    void showInFrames(Player player, ItemFrame[][] itemFrameMatrix, boolean force);

    /**
     * Show this {@link MultiMapController} in {@link ItemFrame}s
     *
     * @param player          {@link Player} that will be able to see the maps
     * @param itemFrameMatrix 2D-Array of {@link ItemFrame}s (<code>ItemFrame[width][height]</code>)
     * @see MapController#showInFrame(Player, ItemFrame)
     */
    void showInFrames(Player player, ItemFrame[][] itemFrameMatrix);

    /**
     * Clear the frames
     *
     * @param player         {@link Player} that will be able to see the cleared frames
     * @param entityIdMatrix 2D-Array of entity-IDs of the {@link ItemFrame}s (<code>int[width][height]</code>)
     */
    void clearFrames(Player player, int[][] entityIdMatrix);

    /**
     * Clear the frames
     *
     * @param player          {@link Player} that will be able to see the cleared frames
     * @param itemFrameMatrix 2D-Array of {@link ItemFrame}s (<code>ItemFrame[width][height]</code>)
     */
    void clearFrames(Player player, ItemFrame[][] itemFrameMatrix);

    interface DebugCallable {
        /**
         * Called to get debug information for a frame
         *
         * @param controller the {@link MapController}
         * @param x          X-Position of the current frame
         * @param y          Y-Position of the current frame
         * @return {@link String} to show when a player looks at the map, or <code>null</code>
         * @see MapController#showInFrame(Player, int, String)
         */
        String call(MapController controller, int x, int y);
    }
}
