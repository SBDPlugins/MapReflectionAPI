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

package tech.sbdevelopment.mapreflectionapi.api.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import tech.sbdevelopment.mapreflectionapi.api.ArrayImage;
import tech.sbdevelopment.mapreflectionapi.api.MapWrapper;

/**
 * This event gets fired when the content of a {@link MapWrapper} is updated
 */
@RequiredArgsConstructor
@Getter
public class MapContentUpdateEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    private final MapWrapper wrapper;
    private final ArrayImage content;
    @Setter
    private boolean sendContent = true;

    /**
     * Construct a new {@link MapContentUpdateEvent}
     *
     * @param wrapper The wrapper that will be updated
     * @param content The content that will be shown
     * @param isAsync Is this event called async?
     */
    public MapContentUpdateEvent(MapWrapper wrapper, ArrayImage content, boolean isAsync) {
        super(isAsync);
        this.wrapper = wrapper;
        this.content = content;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}
