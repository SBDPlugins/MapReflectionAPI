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

import java.io.IOException;
import java.io.InputStream;

/**
 * Input Stream that can also read individual bits
 */
public class BitInputStream extends InputStream {
    private int bitbuff = 0;
    private int bitbuff_len = 0;
    private boolean closed = false;
    private final InputStream input;
    private final boolean closeInput;

    /**
     * Initializes a new Bit Input Stream, reading from the Input Stream specified
     *
     * @param inputStream to read from
     */
    public BitInputStream(InputStream inputStream) {
        this(inputStream, true);
    }

    /**
     * Initializes a new Bit Input Stream, reading from the Input Stream specified
     *
     * @param inputStream      to read from
     * @param closeInputStream whether to close the underlying input stream when closing this stream
     */
    public BitInputStream(InputStream inputStream, boolean closeInputStream) {
        this.input = inputStream;
        this.closeInput = closeInputStream;
    }

    @Override
    public int available() throws IOException {
        if (this.closed) {
            throw new IOException("Stream is closed");
        }
        return this.input.available();
    }

    @Override
    public int read() throws IOException {
        return readBits(8);
    }

    /**
     * Reads bits from the stream
     *
     * @param nBits to read
     * @return read value, -1 when end of stream is reached
     * @throws IOException
     */
    public int readBits(int nBits) throws IOException {
        if (this.closed) {
            throw new IOException("Stream is closed");
        }
        while (this.bitbuff_len < nBits) {
            int readByte = this.input.read();
            if (readByte == -1) return -1;
            this.bitbuff |= (readByte << this.bitbuff_len);
            this.bitbuff_len += 8;
        }
        int result = bitbuff & ((1 << nBits) - 1);
        this.bitbuff >>= nBits;
        this.bitbuff_len -= nBits;
        return result;
    }

    @Override
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            if (this.closeInput) {
                this.input.close();
            }
        }
    }
}
