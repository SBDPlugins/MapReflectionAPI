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

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import tech.sbdevelopment.mapreflectionapi.exceptions.MapLimitExceededException;

import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class MapManager {
    protected final Set<Integer> OCCUPIED_IDS = new HashSet<>();
    private final List<MapWrapper> MANAGED_MAPS = new CopyOnWriteArrayList<>();
    private final Class<?> wrapperClass;

    public MapManager() throws IllegalStateException {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);

        try {
            final Class<?> clazz = Class.forName("tech.sbdevelopment.mapreflectionapi.nms.MapWrapper_" + version);
            if (MapWrapper.class.isAssignableFrom(clazz)) {
                wrapperClass = clazz;
            } else {
                throw new IllegalStateException("Plugin corrupted! Detected invalid MapWrapper class.");
            }
        } catch (Exception ex) {
            throw new IllegalStateException("This Spigot version is not supported! Contact the developer to get support.");
        }
    }

    @Nullable
    public MapWrapper wrapImage(BufferedImage image) {
        return wrapImage(new ArrayImage(image));
    }

    @Nullable
    public MapWrapper wrapImage(ArrayImage image) {
        for (MapWrapper wrapper : MANAGED_MAPS) {
            if (wrapper.getContent().equals(image)) return wrapper;
        }
        return wrapNewImage(image);
    }

    private MapWrapper wrapNewImage(ArrayImage image) {
        try {
            MapWrapper wrapper = (MapWrapper) wrapperClass.getDeclaredConstructor().newInstance();
            MANAGED_MAPS.add(wrapper);
            return wrapper;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            return null;
        }
    }

    public void unwrapImage(MapWrapper wrapper) {
        //TODO Cancel IDs

        wrapper.getController().clearViewers();
        MANAGED_MAPS.remove(wrapper);
    }

    public Set<MapWrapper> getMapsVisibleTo(OfflinePlayer player) {
        Set<MapWrapper> visible = new HashSet<>();
        for (MapWrapper wrapper : MANAGED_MAPS) {
            if (wrapper.getController().isViewing(player)) {
                visible.add(wrapper);
            }
        }
        return visible;
    }

    public MapWrapper getWrapperForId(OfflinePlayer player, int id) {
        for (MapWrapper wrapper : getMapsVisibleTo(player)) {
            if (wrapper.getController().getMapId(player) == id) {
                return wrapper;
            }
        }
        return null;
    }

    public void registerOccupiedID(int id) {
        OCCUPIED_IDS.add(id);
    }

    public void unregisterOccupiedID(int id) {
        OCCUPIED_IDS.remove(id);
    }

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

    public boolean isIdUsedBy(OfflinePlayer player, int id) {
        return id > 0 && getOccupiedIdsFor(player).contains(id);
    }

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

    public void clearAllMapsFor(OfflinePlayer player) {
        for (MapWrapper wrapper : getMapsVisibleTo(player)) {
            wrapper.getController().removeViewer(player);
        }
    }

    public MapWrapper getDuplicate(ArrayImage image) {
        for (MapWrapper wrapper : MANAGED_MAPS) {
            if (image.equals(wrapper.getContent())) {
                return wrapper;
            }
        }
        return null;
    }
}
