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

package tech.sbdevelopment.mapreflectionapi.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import tech.sbdevelopment.mapreflectionapi.api.exceptions.MapLimitExceededException;
import tech.sbdevelopment.mapreflectionapi.managers.Configuration;
import tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtil;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtils.*;

/**
 * The {@link MapManager} manages all the maps. It also contains functions for wrapping.
 */
public class MapManager {
    protected final Set<Integer> occupiedIds = new HashSet<>();
    protected final List<MapWrapper> managedMaps = new CopyOnWriteArrayList<>();

    /**
     * Get the amount of maps managed by the plugin
     *
     * @return The managed maps amount
     */
    public int getManagedMapsCount() {
        return managedMaps.size();
    }

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
            for (MapWrapper wrapper : managedMaps) {
                if (wrapper.getContent().equals(image)) return wrapper;
            }
        }
        return wrapNewImage(image);
    }

    /**
     * Wrap a {@link BufferedImage} and split it into multiple maps
     *
     * @param image   The image to wrap
     * @param rows    Rows of the split (i.e. height)
     * @param columns Columns of the split (i.e. width)
     * @return The wrapper
     */
    public MultiMapWrapper wrapMultiImage(BufferedImage image, int rows, int columns) {
        //Don't add to managedMaps, because the MultiMapWrapper will do that for us
        return new MultiMapWrapper(image, rows, columns);
    }

    /**
     * Wrap an {@link ArrayImage} and split it into multiple maps
     *
     * @param image   The image to wrap
     * @param rows    Rows of the split (i.e. height)
     * @param columns Columns of the split (i.e. width)
     * @return The wrapper
     */
    public MultiMapWrapper wrapMultiImage(ArrayImage image, int rows, int columns) {
        //Don't add to managedMaps, because the MultiMapWrapper will do that for us
        return new MultiMapWrapper(image, rows, columns);
    }

    /**
     * Wrap multiple {@link BufferedImage}s
     *
     * @param images The images to wrap
     * @return The wrapper
     */
    public MultiMapWrapper wrapMultiImage(BufferedImage[][] images) {
        return new MultiMapWrapper(images);
    }

    /**
     * Wrap multiple {@link ArrayImage}s
     *
     * @param images The images to wrap
     * @return The wrapper
     */
    public MultiMapWrapper wrapMultiImage(ArrayImage[][] images) {
        return new MultiMapWrapper(images);
    }

    /**
     * Wrap a new image
     *
     * @param image The image to wrap
     * @return The wrapper
     */
    private MapWrapper wrapNewImage(ArrayImage image) {
        MapWrapper wrapper = new MapWrapper(image);
        managedMaps.add(wrapper);
        return wrapper;
    }

    /**
     * Unwrap an image (will remove the wrapper)
     *
     * @param wrapper The {@link MapWrapper} to unwrap
     */
    public void unwrapImage(MapWrapper wrapper) {
        wrapper.unwrap();
        managedMaps.remove(wrapper);
    }

    /**
     * Get the maps a player can see
     *
     * @param player The {@link Player} to check for
     * @return A {@link Set} with the {@link MapWrapper}s
     */
    public Set<MapWrapper> getMapsVisibleTo(OfflinePlayer player) {
        Set<MapWrapper> visible = new HashSet<>();
        for (MapWrapper wrapper : managedMaps) {
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
     * Get an {@link ItemFrame} by its entity ID
     *
     * @param world    The world the {@link ItemFrame} is in
     * @param entityId Entity-ID of the {@link ItemFrame}
     * @return The found {@link ItemFrame}, or <code>null</code>
     */
    public ItemFrame getItemFrameById(World world, int entityId) {
        Object worldHandle = getHandle(world);
        Object nmsEntity = ReflectionUtil.callMethod(worldHandle, supports(18) ? "a" : "getEntity", entityId);
        if (nmsEntity == null) return null;

        Object craftEntity = ReflectionUtil.callMethod(nmsEntity, "getBukkitEntity");
        if (craftEntity == null) return null;

        Class<?> itemFrameClass = getNMSClass("world.entity.decoration", "EntityItemFrame");
        if (itemFrameClass == null) return null;

        if (craftEntity.getClass().isAssignableFrom(itemFrameClass))
            return (ItemFrame) itemFrameClass.cast(craftEntity);

        return null;
    }

    /**
     * Register an occupied map ID
     *
     * @param id The map ID to register
     */
    public void registerOccupiedID(int id) {
        occupiedIds.add(id);
    }

    /**
     * Unregister an occupied map ID
     *
     * @param id The map ID to unregister
     */
    public void unregisterOccupiedID(int id) {
        occupiedIds.remove(id);
    }

    /**
     * Get the occupied IDs for a player
     *
     * @param player The {@link OfflinePlayer} to check for
     * @return A {@link Set} with the found map IDs
     */
    public Set<Integer> getOccupiedIdsFor(OfflinePlayer player) {
        Set<Integer> ids = new HashSet<>();
        for (MapWrapper wrapper : managedMaps) {
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
        occupied.addAll(occupiedIds);

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
        for (MapWrapper wrapper : managedMaps) {
            if (image.equals(wrapper.getContent())) {
                return wrapper;
            }
        }
        return null;
    }
}
