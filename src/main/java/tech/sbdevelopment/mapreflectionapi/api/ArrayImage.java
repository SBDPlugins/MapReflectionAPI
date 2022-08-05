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
