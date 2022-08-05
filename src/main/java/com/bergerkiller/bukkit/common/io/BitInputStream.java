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
