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

package com.bergerkiller.bukkit.common.map.color;

import com.bergerkiller.bukkit.common.io.BitInputStream;
import com.bergerkiller.bukkit.common.io.BitPacket;

import java.io.IOException;

/**
 * Encodes or decodes a 256x256 grid of booleans by walking down the connected lines and encoding them
 * using drawing instructions. For example, a diagonal line in the grid may be encoded as follows:
 * <ul>
 * <li>SET_POSITION(23, 56)</li>
 * <li>SET_DX(-1)</li>
 * <li>SET_DY(1)</li>
 * <li>MOVE DX AND DRAW</li>
 * <li>MOVE DX AND DRAW</li>
 * <li>MOVE DY AND DRAW</li>
 * <li>MOVE DX AND DRAW</li>
 * <li>MOVE DX AND DRAW</li>
 * <li>MOVE DY AND DRAW</li>
 * <li>etc.</li>
 * </ul>
 * <p>
 * For encoding the data, the follow bits are written out in sequence:
 * <ul>
 * <li>00 -> MOVE DX AND DRAW</li>
 * <li>01 -> MOVE DY AND DRAW</li>
 * <li>10 -> MOVE DX+DY AND DRAW</li>
 * <li>11 100 -> SET DX = -1</li>
 * <li>11 101 -> SET DX = 1</li>
 * <li>11 110 -> SET DY = -1</li>
 * <li>11 111 -> SET DY = 1</li>
 * <li>11 00 [byte_x][byte_y] -> SET POSITION AND DRAW</li>
 * <li>11 01 -> STOP</li>
 * </ul>
 */
public class MCSDWebbingCodec {
    private int last_x, last_y;
    private int last_dx, last_dy;
    public boolean[] strands = new boolean[1 << 16];
    private final BitPacket[] packets = new BitPacket[1024];

    public MCSDWebbingCodec() {
        for (int i = 0; i < this.packets.length; i++) {
            this.packets[i] = new BitPacket();
        }
    }

    public void reset(boolean[] cells, boolean copyCells) {
        if (copyCells) {
            System.arraycopy(cells, 0, this.strands, 0, cells.length);
        } else {
            this.strands = cells;
        }
        this.last_x = -1000;
        this.last_y = -1000;
        this.last_dx = 1;
        this.last_dy = 1;
    }

    public boolean readNext(BitInputStream stream) throws IOException {
        int op = stream.readBits(2);
        if (op == 0b11) {
            if (stream.readBits(1) == 1) {
                // Set DX/DY increment/decrement
                int sub = stream.readBits(2);
                if (sub == 0b00) {
                    last_dx = -1;
                } else if (sub == 0b01) {
                    last_dx = 1;
                } else if (sub == 0b10) {
                    last_dy = -1;
                } else if (sub == 0b11) {
                    last_dy = 1;
                }
            } else {
                // Command codes
                if (stream.readBits(1) == 1) {
                    // End of slice
                    return false;
                } else {
                    // Reset position
                    last_x = stream.readBits(8);
                    last_y = stream.readBits(8);
                    strands[last_x | (last_y << 8)] = true;
                }
            }
        } else {
            // Write next pixel
            if (op == 0b00) {
                last_x += last_dx;
            } else if (op == 0b01) {
                last_y += last_dy;
            } else if (op == 0b10) {
                last_x += last_dx;
                last_y += last_dy;
            } else if (op == -1) {
                // End of stream
                return false;
            }
            strands[last_x | (last_y << 8)] = true;
        }
        return true;
    }
}
