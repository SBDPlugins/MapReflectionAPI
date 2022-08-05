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

package com.bergerkiller.bukkit.common.map.color;

import com.bergerkiller.bukkit.common.io.BitInputStream;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.InflaterInputStream;

/**
 * Stores all map color space information in a highly compressed bubble format.
 * In this format it is assumed the color data is in cell shapes. It stores the cell
 * borders separate from the colors using the {@link MCSDWebbingCodec}. These cells
 * are then filled with colors to reproduce the original image.
 */
public class MCSDBubbleFormat extends MapColorSpaceData {
    public final boolean[][] strands = new boolean[256][256 * 256];
    public final List<Bubble> bubbles = new ArrayList<>();

    public void readFrom(InputStream stream) throws IOException {
        try (BitInputStream bitStream = new BitInputStream(new InflaterInputStream(stream))) {
            // Read all color RGB values
            for (int i = 0; i < 256; i++) {
                int r = bitStream.read();
                int g = bitStream.read();
                int b = bitStream.read();
                int a = bitStream.read();
                this.setColor((byte) i, new Color(r, g, b, a));
            }

            // Read all bubbles from the stream
            while (true) {
                Bubble bubble = new Bubble();
                bubble.color = (byte) bitStream.read();
                if (bubble.color == 0) {
                    break;
                }
                bubble.x = bitStream.read();
                bubble.y = bitStream.read();
                bubble.z_min = bitStream.read();
                bubble.z_max = bubble.z_min + bitStream.read();
                this.bubbles.add(bubble);
            }

            // Read bubble boundary information from the stream
            MCSDWebbingCodec codec = new MCSDWebbingCodec();
            for (int z = 0; z < 256; z++) {
                Arrays.fill(this.strands[z], false);
                codec.reset(strands[z], false);
                while (codec.readNext(bitStream)) ;
            }

            // Initialize the colors with the bubble colors
            this.initColors();

            // Read color correction data for pixels unset (value = 0)
            for (int i = 0; i < (1 << 24); i++) {
                if (this.get(i) == 0) {
                    if (bitStream.readBits(1) == 0) {
                        this.set(i, this.get(i - 1));
                    } else {
                        int mode = bitStream.readBits(2);
                        if (mode == 0) {
                            this.set(i, this.get(i - 256));
                        } else if (mode == 1) {
                            this.set(i, this.get(i + 1));
                        } else if (mode == 2) {
                            this.set(i, this.get(i + 256));
                        } else {
                            this.set(i, (byte) bitStream.readBits(8));
                        }
                    }
                }
            }
        }
    }

    private void initColors() {
        // Set initial cell colors
        this.clearRGBData();
        for (Bubble cell : bubbles) {
            for (int z = cell.z_min; z <= cell.z_max; z++) {
                this.set(cell.x, cell.y, z, cell.color);
            }
        }
        spreadColors();
    }

    private void spreadColors() {
        final boolean[] all_strands = new boolean[1 << 24];
        for (int z = 0; z < 256; z++) {
            System.arraycopy(this.strands[z], 0, all_strands, z << 16, 1 << 16);
        }

        boolean mode = false;
        boolean hasChanges;
        do {
            hasChanges = false;

            // Alternate the direction in which we process every step
            // This prevents really slow filling when the direction is 'wrong'
            // The below logic is partially based on the light fixing algorithm in Light Cleaner
            final int index_end, index_delta;
            int index;
            byte color;
            if (mode = !mode) {
                index_delta = 1;
                index = 0;
                index_end = (1 << 24);
            } else {
                index_delta = -1;
                index = (1 << 24) - 1;
                index_end = 0;
            }
            do {
                if (!all_strands[index]) {
                    all_strands[index] = true;

                    if ((index & 0xFF) < 0xFF) {
                        if ((color = this.get(index + 1)) != 0) {
                            this.set(index, color);
                            hasChanges = true;
                        } else if ((color = this.get(index)) != 0) {
                            this.set(index + 1, color);
                            hasChanges = true;
                        } else {
                            all_strands[index] = false; // retry
                        }
                    }

                    if ((index & 0xFF00) < 0xFF00) {
                        if ((color = this.get(index + 256)) != 0) {
                            this.set(index, color);
                            hasChanges = true;
                        } else if ((color = this.get(index)) != 0) {
                            this.set(index + 256, color);
                            hasChanges = true;
                        } else {
                            all_strands[index] = false; // retry
                        }
                    }
                }
            } while ((index += index_delta) != index_end);
        } while (hasChanges);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof MCSDBubbleFormat) {
            MCSDBubbleFormat other = (MCSDBubbleFormat) o;
            for (int i = 0; i < strands.length; i++) {
                if (other.strands[i] != this.strands[i]) {
                    return false;
                }
            }
            if (bubbles.size() != other.bubbles.size()) {
                return false;
            }
            for (int i = 0; i < bubbles.size(); i++) {
                if (!bubbles.get(i).equals(other.bubbles.get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static class Bubble {
        public int x, y;
        public int z_min;
        public int z_max;
        public byte color;

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o instanceof Bubble) {
                Bubble other = (Bubble) o;
                return other.x == x && other.y == y &&
                        other.z_min == z_min && other.z_max == z_max &&
                        other.color == color;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "cell{x=" + x + ", y=" + y + ", zmin=" + z_min + ", zmax=" + z_max + ", color=" + (color & 0xFF) + "}";
        }
    }

}
