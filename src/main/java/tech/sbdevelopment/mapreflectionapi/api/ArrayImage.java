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

import com.bergerkiller.bukkit.common.map.MapColorPalette;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * An {@link ArrayImage} contains an image converted to a Minecraft byte array.
 */
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class ArrayImage {
    public final byte[] array;
    public int minX = 0;
    public int minY = 0;
    public int maxX = 128;
    public int maxY = 128;
    private int width;
    private int height;
    private int imageType = BufferedImage.TYPE_4BYTE_ABGR;

    /**
     * Convert a {@link BufferedImage} to an ArrayImage
     *
     * @param image image to convert
     */
    public ArrayImage(BufferedImage image) {
        this.imageType = image.getType();

        this.width = image.getWidth();
        this.height = image.getHeight();

        BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = temp.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        int[] pixels = new int[temp.getWidth() * temp.getHeight()];
        temp.getRGB(0, 0, temp.getWidth(), temp.getHeight(), pixels, 0, temp.getWidth());

        byte[] result = new byte[temp.getWidth() * temp.getHeight()];
        for (int i = 0; i < pixels.length; i++) {
            result[i] = MapColorPalette.getColor(new Color(pixels[i], true));
        }

        this.array = result;
    }

    /**
     * Get the {@link BufferedImage} of this ArrayImage
     *
     * @return The converted image
     */
    public BufferedImage toBuffered() {
        BufferedImage img = new BufferedImage(width, height, this.imageType);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                img.setRGB(x, y, MapColorPalette.getRealColor(array[y * width + x]).getRGB());
            }
        }
        return img;
    }
}
