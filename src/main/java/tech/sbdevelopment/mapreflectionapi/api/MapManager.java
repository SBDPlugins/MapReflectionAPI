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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import tech.sbdevelopment.mapreflectionapi.api.exceptions.MapLimitExceededException;
import tech.sbdevelopment.mapreflectionapi.managers.Configuration;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class MapManager {
    protected final Set<Integer> OCCUPIED_IDS = new HashSet<>();
    private final List<MapWrapper> MANAGED_MAPS = new CopyOnWriteArrayList<>();

    /**
     * Wrap a {@link BufferedImage} in a {@link MapWrapper}
     *
     * @param image The image to wrap
     * @return The wrapper
     */
    public MapWrapper wrapImage(BufferedImage image) {
        return wrapImage(new ArrayImage(image));
    }

    /**
     * Wrap a {@link ArrayImage} in a {@link MapWrapper}
     *
     * @param image The image to wrap
     * @return The wrapper
     */
    public MapWrapper wrapImage(ArrayImage image) {
        if (Configuration.getInstance().isImageCache()) {
            for (MapWrapper wrapper : MANAGED_MAPS) {
                if (wrapper.getContent().equals(image)) return wrapper;
            }
        }
        return wrapNewImage(image);
    }

    /**
     * Wrap a new image
     *
     * @param image The image to wrap
     * @return The wrapper
     */
    private MapWrapper wrapNewImage(ArrayImage image) {
        MapWrapper wrapper = new MapWrapper(image);
        MANAGED_MAPS.add(wrapper);
        return wrapper;
    }

    /**
     * Unwrap an image (will remove the wrapper)
     *
     * @param wrapper The {@link MapWrapper} to unwrap
     */
    public void unwrapImage(MapWrapper wrapper) {
        wrapper.controller.cancelSend();
        wrapper.getController().clearViewers();
        MANAGED_MAPS.remove(wrapper);
    }

    /**
     * Get the maps a player can see
     *
     * @param player The {@link Player} to check for
     * @return A {@link Set} with the {@link MapWrapper}s
     */
    public Set<MapWrapper> getMapsVisibleTo(OfflinePlayer player) {
        Set<MapWrapper> visible = new HashSet<>();
        for (MapWrapper wrapper : MANAGED_MAPS) {
            if (wrapper.getController().isViewing(player)) {
                visible.add(wrapper);
            }
        }
        return visible;
    }

    /**
     * Get the wrapper by a player and map id
     *
     * @param player The {@link OfflinePlayer} to check for
     * @param id     The ID of the map
     * @return The {@link MapWrapper} for that map or null
     */
    @Nullable
    public MapWrapper getWrapperForId(OfflinePlayer player, int id) {
        for (MapWrapper wrapper : getMapsVisibleTo(player)) {
            if (wrapper.getController().getMapId(player) == id) {
                return wrapper;
            }
        }
        return null;
    }

    /**
     * Register an occupied map ID
     *
     * @param id The map ID to register
     */
    public void registerOccupiedID(int id) {
        OCCUPIED_IDS.add(id);
    }

    /**
     * Unregister an occupied map ID
     *
     * @param id The map ID to unregister
     */
    public void unregisterOccupiedID(int id) {
        OCCUPIED_IDS.remove(id);
    }

    /**
     * Get the occupied IDs for a player
     *
     * @param player The {@link OfflinePlayer} to check for
     * @return A {@link Set} with the found map IDs
     */
    public Set<Integer> getOccupiedIdsFor(OfflinePlayer player) {
        Set<Integer> ids = new HashSet<>();
        for (MapWrapper wrapper : MANAGED_MAPS) {
            int s = wrapper.getController().getMapId(player);
            if (s >= 0) {
                ids.add(s);
            }
        }
        return ids;
    }

    /**
     * Check if a player uses a map ID
     *
     * @param player The {@link OfflinePlayer} to check for
     * @param id     The map ID to check for
     * @return true/false
     */
    public boolean isIdUsedBy(OfflinePlayer player, int id) {
        return id > 0 && getOccupiedIdsFor(player).contains(id);
    }

    /**
     * Get the next ID that can be used for this player
     *
     * @param player The {@link Player} to check for
     * @return The next ID
     * @throws MapLimitExceededException If no IDs are available
     */
    public int getNextFreeIdFor(Player player) throws MapLimitExceededException {
        Set<Integer> occupied = getOccupiedIdsFor(player);
        //Add the 'default' occupied IDs
        occupied.addAll(OCCUPIED_IDS);

        int largest = 0;
        for (Integer s : occupied) {
            if (s > largest) {
                largest = s;
            }
        }

        //Simply increase the maximum id if it's still small enough
        if (largest + 1 < Integer.MAX_VALUE) {
            return largest + 1;
        }

        //Otherwise iterate through all options until there is an unused id
        for (int s = 0; s < Integer.MAX_VALUE; s++) {
            if (!occupied.contains(s)) {
                return s;
            }
        }

        //If we end up here, this player has no more free ids. Let's hope nobody uses this many Maps.
        throw new MapLimitExceededException("'" + player + "' reached the maximum amount of available Map-IDs");
    }

    /**
     * Clear all the maps of a player
     * This makes them no longer viewable for this player
     *
     * @param player The {@link OfflinePlayer} to clear for
     */
    public void clearAllMapsFor(OfflinePlayer player) {
        for (MapWrapper wrapper : getMapsVisibleTo(player)) {
            wrapper.getController().removeViewer(player);
        }
    }

    /**
     * Check if a MapWrapper exists for this image
     * If so, the same MapWrapper can be used
     *
     * @param image The {@link ArrayImage} to check for
     * @return A {@link MapWrapper} if duplicate, or null if not
     */
    @Nullable
    public MapWrapper getDuplicate(ArrayImage image) {
        for (MapWrapper wrapper : MANAGED_MAPS) {
            if (image.equals(wrapper.getContent())) {
                return wrapper;
            }
        }
        return null;
    }
}
