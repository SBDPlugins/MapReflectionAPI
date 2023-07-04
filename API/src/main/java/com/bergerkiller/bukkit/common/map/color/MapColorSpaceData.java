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

package com.bergerkiller.bukkit.common.map.color;

import java.awt.*;
import java.util.Arrays;

/**
 * Stores the raw map color space data, enabling transformation between different storage methods.
 */
public class MapColorSpaceData implements Cloneable {
    private final Color[] colors = new Color[256];
    private final byte[] data = new byte[1 << 24];

    public MapColorSpaceData() {
        Arrays.fill(this.colors, new Color(0, 0, 0, 0));
    }

    /**
     * Clears only the RGB data. Equivalent to using {@link #set(int, byte)} on all RGB colors.
     */
    public final void clearRGBData() {
        Arrays.fill(this.data, (byte) 0);
    }

    /**
     * Clears all data, setting all colors to transparent
     */
    public final void clear() {
        Arrays.fill(this.colors, new Color(0, 0, 0, 0));
        Arrays.fill(this.data, (byte) 0);
    }

    /**
     * Sets all color data of this color space data to that from the input color space data
     *
     * @param data to set
     */
    public void readFrom(MapColorSpaceData data) {
        System.arraycopy(data.data, 0, this.data, 0, this.data.length);
        System.arraycopy(data.colors, 0, this.colors, 0, this.colors.length);
    }

    /**
     * Sets a single map palette color
     *
     * @param code  of the color
     * @param color to set to
     */
    public final void setColor(byte code, Color color) {
        this.colors[code & 0xFF] = color;
    }

    /**
     * Gets a single map palette color
     *
     * @param code of the color
     * @return map palette color
     */
    public final Color getColor(byte code) {
        return this.colors[code & 0xFF];
    }

    /**
     * Sets the map color code value for an rgb value
     *
     * @param r    component
     * @param g    component
     * @param b    component
     * @param code to set to
     */
    public final void set(int r, int g, int b, byte code) {
        this.data[getDataIndex(r, g, b)] = code;
    }

    /**
     * Gets the map color code value for an rgb value
     *
     * @param r component
     * @param g component
     * @param b component
     * @return color code
     */
    public final byte get(int r, int g, int b) {
        return this.data[getDataIndex(r, g, b)];
    }

    /**
     * Sets the map color code for an rgb value
     *
     * @param index rgb compound value
     * @param code  to set to
     */
    public final void set(int index, byte code) {
        this.data[index] = code;
    }

    /**
     * Gets the map color code for an rgb value
     *
     * @param index rgb compound value
     * @return color code
     */
    public final byte get(int index) {
        return this.data[index];
    }

    @Override
    public MapColorSpaceData clone() {
        MapColorSpaceData clone = new MapColorSpaceData();
        System.arraycopy(this.colors, 0, clone.colors, 0, this.colors.length);
        System.arraycopy(this.data, 0, clone.data, 0, this.data.length);
        return clone;
    }

    /**
     * Gets the mapping index of an rgb value
     *
     * @param r component
     * @param g component
     * @param b component
     * @return index
     */
    private static int getDataIndex(byte r, byte g, byte b) {
        return (r & 0xFF) + ((g & 0xFF) << 8) + ((b & 0xFF) << 16);
    }

    /**
     * Gets the mapping index of an rgb value
     *
     * @param r component
     * @param g component
     * @param b component
     * @return index
     */
    private static int getDataIndex(int r, int g, int b) {
        return (r & 0xFF) + ((g & 0xFF) << 8) + ((b & 0xFF) << 16);
    }
}
