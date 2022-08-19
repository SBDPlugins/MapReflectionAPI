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

package com.bergerkiller.bukkit.common.io;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple container for multiple bits of data.
 */
@EqualsAndHashCode
@ToString
public class BitPacket implements Cloneable {
    public int data, bits;

    public BitPacket() {
        this.data = 0;
        this.bits = 0;
    }

    public BitPacket(int data, int bits) {
        this.data = data;
        this.bits = bits;
    }

    @Override
    public BitPacket clone() {
        return new BitPacket(this.data, this.bits);
    }
}
