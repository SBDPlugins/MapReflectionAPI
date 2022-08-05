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

package com.bergerkiller.bukkit.common.map;

import com.bergerkiller.bukkit.common.map.color.MCSDBubbleFormat;
import com.bergerkiller.bukkit.common.map.color.MCSDGenBukkit;
import com.bergerkiller.bukkit.common.map.color.MapColorSpaceData;
import tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtil;

import java.awt.*;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Additional functionality on top of Bukkit's MapPalette
 */
public class MapColorPalette {
    private static final MapColorSpaceData COLOR_MAP_DATA;
    public static final byte[] COLOR_MAP_AVERAGE = new byte[0x10000];
    public static final byte[] COLOR_MAP_ADD = new byte[0x10000];
    public static final byte[] COLOR_MAP_SUBTRACT = new byte[0x10000];
    public static final byte[] COLOR_MAP_MULTIPLY = new byte[0x10000];
    public static final byte[] COLOR_MAP_SPECULAR = new byte[0x10000];

    public static final byte COLOR_TRANSPARENT = 0;

    static {
        // Now we know java.awt exists we can initialize this one
        COLOR_MAP_DATA = new MapColorSpaceData();

        // Load color map data from the Bubble format file bundled with the library
        {
            boolean success = false;
            MCSDBubbleFormat bubbleData = new MCSDBubbleFormat();
            try {
                String bub_path_postfix;
                if (ReflectionUtil.supports(17)) {
                    bub_path_postfix = "map_1_17.bub";
                } else if (ReflectionUtil.supports(16)) {
                    bub_path_postfix = "map_1_16.bub";
                } else if (ReflectionUtil.supports(12)) {
                    bub_path_postfix = "map_1_12.bub";
                } else {
                    bub_path_postfix = "map_1_8_8.bub";
                }
                String bub_path = "/tech/sbdevelopment/mapreflectionapi/libs/bkcommonlib/internal/resources/map/" + bub_path_postfix;
                InputStream input = MapColorPalette.class.getResourceAsStream(bub_path);
                if (input != null) {
                    bubbleData.readFrom(input);
                    success = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (success) {
                COLOR_MAP_DATA.readFrom(bubbleData);
            } else {
                MCSDGenBukkit bukkitGen = new MCSDGenBukkit();
                bukkitGen.generate();
                COLOR_MAP_DATA.readFrom(bukkitGen);
            }
        }

        // Generate 256 lightness values for all colors
        for (int a = 0; a < 256; a++) {
            int index = (a * 256);
            Color color_a = getRealColor((byte) a);
            if (color_a.getAlpha() < 128) {
                // All specular colors for the transparent color are transparent
                Arrays.fill(COLOR_MAP_SPECULAR, index, index + 256, COLOR_TRANSPARENT);
            } else {
                for (int b = 0; b < 256; b++) {
                    // 0.0 = black
                    // 1.0 = natural color
                    // 2.0 = white
                    float f = b / 128.0f;
                    int sr = (int) (color_a.getRed() * f);
                    int sg = (int) (color_a.getGreen() * f);
                    int sb = (int) (color_a.getBlue() * f);
                    COLOR_MAP_SPECULAR[index++] = getColor(sr, sg, sb);
                }
            }
        }

        // Initialize the color map tables for all possible color values
        for (int c1 = 0; c1 < 256; c1++) {
            for (int c2 = 0; c2 < 256; c2++) {
                initTable((byte) c1, (byte) c2);
            }
        }
    }

    private static void initTable(byte color1, byte color2) {
        int index = getMapIndex(color1, color2);
        if (isTransparent(color1) || isTransparent(color2)) {
            initTransparent(index, color2);
        } else {
            Color c1 = getRealColor(color1);
            Color c2 = getRealColor(color2);
            initColor(
                    index,
                    c1.getRed(), c1.getGreen(), c1.getBlue(),
                    c2.getRed(), c2.getGreen(), c2.getBlue()
            );
        }
    }

    private static void initTransparent(int index, byte color2) {
        COLOR_MAP_AVERAGE[index] = color2;
        COLOR_MAP_ADD[index] = color2;
        COLOR_MAP_SUBTRACT[index] = color2;
        COLOR_MAP_MULTIPLY[index] = (byte) 0;
    }

    private static void initColor(int index, int r1, int g1, int b1, int r2, int g2, int b2) {
        initArray(COLOR_MAP_AVERAGE, index, (r1 + r2) >> 1, (g1 + g2) >> 1, (b1 + b2) >> 1);
        initArray(COLOR_MAP_ADD, index, (r1 + r2), (g1 + g2), (b1 + b2));
        initArray(COLOR_MAP_SUBTRACT, index, (r2 - r1), (g2 - g1), (b2 - b1));
        initArray(COLOR_MAP_MULTIPLY, index, (r1 * r2) / 255, (g1 * g2) / 255, (b1 * b2) / 255);
    }

    private static void initArray(byte[] array, int index, int r, int g, int b) {
        if (r < 0x00) r = 0x00;
        if (r > 0xFF) r = 0xFF;
        if (g < 0x00) g = 0x00;
        if (g > 0xFF) g = 0xFF;
        if (b < 0x00) b = 0x00;
        if (b > 0xFF) b = 0xFF;
        array[index] = getColor(r, g, b);
    }

    /**
     * Gets whether a particular color code is a transparent color.
     * There are 4 transparent colors available. Usually value 0 is used.
     *
     * @param color value
     * @return True if transparent
     */
    public static boolean isTransparent(byte color) {
        return (color & 0xFF) < 0x4;
    }

    /**
     * Gets the Minecraft map color code for an RGB color
     *
     * @param color input
     * @return minecraft color
     */
    public static byte getColor(Color color) {
        if ((color.getAlpha() & 0x80) == 0) {
            return COLOR_TRANSPARENT;
        } else {
            return COLOR_MAP_DATA.get(color.getRed(), color.getGreen(), color.getBlue());
        }
    }

    /**
     * Gets the Minecraft map color code for an RGB color
     *
     * @param r - red component
     * @param g - green component
     * @param b - blue component
     * @return minecraft color
     */
    public static byte getColor(int r, int g, int b) {
        // This helps prevent dumb exceptions.
        // Nobody likes random exceptions when all you're doing is color calculations
        if (r < 0)
            r = 0;
        else if (r > 255)
            r = 255;
        if (g < 0)
            g = 0;
        else if (g > 255)
            g = 255;
        if (b < 0)
            b = 0;
        else if (b > 255)
            b = 255;

        return COLOR_MAP_DATA.get(r, g, b);
    }

    /**
     * Gets the index into one of the palette remap arrays
     *
     * @param color_a first color
     * @param color_b second color
     * @return index
     */
    public static int getMapIndex(byte color_a, byte color_b) {
        return (color_a & 0xFF) | ((color_b & 0xFF) << 8);
    }

    /**
     * Gets the real RGB color belonging to a color code
     *
     * @param color code input
     * @return real RGB color
     */
    public static Color getRealColor(byte color) {
        return COLOR_MAP_DATA.getColor(color);
    }
}
