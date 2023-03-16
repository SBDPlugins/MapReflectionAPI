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

import com.bergerkiller.bukkit.common.io.BitInputStream;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
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
        for (MCSDBubbleFormat.Bubble cell : bubbles) {
            for (int z = cell.z_min; z <= cell.z_max; z++) {
                this.set(cell.x, cell.y, z, cell.color);
            }
        }
        spreadColors();
    }

    private void spreadColors() {
        // As we'll be processing pretty much every element, allocate the full space (60MB)
        // The range of the buffer we process shrinks as we spread
        StrandBuffer buf;
        {
            final int[] buffer = new int[1 << 24];
            int count = -1;
            for (int z = 0; z < 256; z++) {
                boolean[] layerStrands = this.strands[z];
                int indexOffset = z << 16;
                for (int i = 0; i < (1 << 16); i++) {
                    if (!layerStrands[i]) {
                        buffer[++count] = indexOffset + i;
                    }
                }
            }
            count++;
            buf = new StrandBuffer(buffer, count);
        }

        // Process all until no more changes remain
        buf.process(index -> {
            byte color;

            boolean col = ((index & 0xFF) < 0xFF);
            boolean row = ((index & 0xFF00) < 0xFF00);

            if (col && row) {
                if ((color = this.get(index)) != 0) {
                    this.set(index + 1, color);
                    this.set(index + 256, color);
                    return true;
                } else if ((color = this.get(index + 1)) != 0) {
                    this.set(index, color);
                    this.set(index + 256, color);
                    return true;
                } else if ((color = this.get(index + 256)) != 0) {
                    this.set(index, color);
                    this.set(index + 1, color);
                    return true;
                }
            } else if (col) {
                if ((color = this.get(index)) != 0) {
                    this.set(index + 1, color);
                    return true;
                } else if ((color = this.get(index + 1)) != 0) {
                    this.set(index, color);
                    return true;
                }
            } else if (row) {
                if ((color = this.get(index)) != 0) {
                    this.set(index + 256, color);
                    return true;
                } else if ((color = this.get(index + 256)) != 0) {
                    this.set(index, color);
                    return true;
                }
            }

            return false;
        });
    }

    private static class StrandBuffer {
        private final int[] buf;
        private int start, end;

        public StrandBuffer(int[] buffer, int count) {
            this.buf = buffer;
            this.start = 0;
            this.end = count - 1;
        }

        public void process(IntPredicate strandIndexProc) {
            while (forward(strandIndexProc) && reverse(strandIndexProc)) {
                // Process alternating over and over until there are no more changes
            }
        }

        public boolean forward(IntPredicate strandIndexProc) {
            int[] buf = this.buf;
            int writeIdx = start - 1;
            int endIdx = end;
            boolean changed = false;
            for (int i = start; i <= endIdx; ++i) {
                int strandIndex = buf[i];
                if (strandIndexProc.test(strandIndex)) {
                    changed = true;
                } else {
                    buf[++writeIdx] = strandIndex;
                }
            }
            this.end = writeIdx;
            return changed;
        }

        public boolean reverse(IntPredicate strandIndexProc) {
            int[] buf = this.buf;
            int writeIdx = end + 1;
            int startIdx = start;
            boolean changed = false;
            for (int i = end; i >= startIdx; --i) {
                int strandIndex = buf[i];
                if (strandIndexProc.test(strandIndex)) {
                    changed = true;
                } else {
                    buf[--writeIdx] = strandIndex;
                }
            }
            this.start = writeIdx;
            return changed;
        }
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
