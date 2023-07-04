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

package tech.sbdevelopment.mapreflectionapi.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.api.exceptions.MapLimitExceededException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static tech.sbdevelopment.mapreflectionapi.utils.MainUtil.validateArrayDimensions;

/**
 * A {@link MultiMapWrapper} wraps one image split in pieces.
 */
public class MultiMapWrapper extends AbstractMapWrapper {
    private final MapWrapper[][] wrapperMatrix;

    public MultiMapWrapper(BufferedImage image, int rows, int columns) {
        this(splitImage(image, columns, rows));
    }

    public MultiMapWrapper(ArrayImage image, int rows, int columns) {
        this(splitImage(image.toBuffered(), columns, rows));
    }

    public MultiMapWrapper(ArrayImage[][] imageMatrix) {
        wrapperMatrix = new MapWrapper[imageMatrix.length][imageMatrix[0].length];

        for (int x = 0; x < imageMatrix.length; x++) {
            if (imageMatrix[x].length != imageMatrix[0].length) {
                throw new IllegalArgumentException("An image in a MultiMapWrapper is not rectangular!");
            }

            for (int y = 0; y < imageMatrix[x].length; y++) {
                wrapperMatrix[x][y] = MapReflectionAPI.getMapManager().wrapImage(imageMatrix[x][y]);
            }
        }
    }

    public MultiMapWrapper(BufferedImage[][] imageMatrix) {
        wrapperMatrix = new MapWrapper[imageMatrix.length][imageMatrix[0].length];

        for (int x = 0; x < imageMatrix.length; x++) {
            if (imageMatrix[x].length != imageMatrix[0].length) {
                throw new IllegalArgumentException("An image in a MultiMapWrapper is not rectangular!");
            }

            for (int y = 0; y < imageMatrix[x].length; y++) {
                wrapperMatrix[x][y] = MapReflectionAPI.getMapManager().wrapImage(imageMatrix[x][y]);
            }
        }
    }

    private final MultiMapController controller = new MultiMapController() {
        private final Set<UUID> viewers = new HashSet<>();

        @Override
        public void addViewer(Player player) throws MapLimitExceededException {
            if (!viewers.contains(player.getUniqueId())) {
                for (MapWrapper[] mapWrappers : wrapperMatrix) {
                    for (MapWrapper wrapper : mapWrappers) {
                        wrapper.getController().addViewer(player);
                    }
                }
                viewers.add(player.getUniqueId());
            }
        }

        @Override
        public void removeViewer(OfflinePlayer player) {
            for (MapWrapper[] mapWrappers : wrapperMatrix) {
                for (MapWrapper wrapper : mapWrappers) {
                    wrapper.getController().removeViewer(player);
                }
            }
            viewers.remove(player.getUniqueId());
        }

        @Override
        public void clearViewers() {
            for (MapWrapper[] mapWrappers : wrapperMatrix) {
                for (MapWrapper wrapper : mapWrappers) {
                    wrapper.getController().clearViewers();
                }
            }
            viewers.clear();
        }

        @Override
        public boolean isViewing(OfflinePlayer player) {
            return viewers.contains(player.getUniqueId());
        }

        @Override
        public void update(@NotNull ArrayImage content) {
            ArrayImage[][] split = splitImage(content.toBuffered(), wrapperMatrix[0].length, wrapperMatrix.length);
            for (int x = 0; x < wrapperMatrix.length; x++) {
                for (int y = 0; y < wrapperMatrix[x].length; y++) {
                    wrapperMatrix[x][y].getController().update(split[x][y]);
                }
            }
        }

        @Override
        public void sendContent(Player player) {
            sendContent(player, false);
        }

        @Override
        public void sendContent(Player player, boolean withoutQueue) {
            for (MapWrapper[] mapWrappers : wrapperMatrix) {
                for (MapWrapper wrapper : mapWrappers) {
                    wrapper.getController().sendContent(player, withoutQueue);
                }
            }
        }

        @Override
        public void cancelSend() {
            for (MapWrapper[] mapWrappers : wrapperMatrix) {
                for (MapWrapper wrapper : mapWrappers) {
                    wrapper.getController().cancelSend();
                }
            }
        }

        @Override
        public void showInFrames(Player player, Integer[][] entityIdMatrix) {
            validateArrayDimensions(wrapperMatrix, entityIdMatrix);

            for (int x = 0; x < entityIdMatrix.length; x++) {
                for (int y = 0; y < entityIdMatrix[x].length; y++) {
                    wrapperMatrix[y][x].getController().showInFrame(player, entityIdMatrix[x][wrapperMatrix.length - 1 - y]);
                }
            }
        }

        @Override
        public void showInFrames(Player player, Integer[][] entityIdMatrix, DebugCallable callable) {
            validateArrayDimensions(wrapperMatrix, entityIdMatrix);

            for (int x = 0; x < entityIdMatrix.length; x++) {
                for (int y = 0; y < entityIdMatrix[x].length; y++) {
                    wrapperMatrix[y][x].getController().showInFrame(player, entityIdMatrix[x][wrapperMatrix.length - 1 - y], callable.call(wrapperMatrix[y][x].getController(), x, y));
                }
            }
        }

        @Override
        public void showInFrames(Player player, ItemFrame[][] itemFrameMatrix, boolean force) {
            validateArrayDimensions(wrapperMatrix, itemFrameMatrix);

            for (int x = 0; x < itemFrameMatrix.length; x++) {
                for (int y = 0; y < itemFrameMatrix[x].length; y++) {
                    wrapperMatrix[y][x].getController().showInFrame(player, itemFrameMatrix[x][wrapperMatrix.length - 1 - y], force);
                }
            }
        }

        @Override
        public void showInFrames(Player player, ItemFrame[][] itemFrameMatrix) {
            showInFrames(player, itemFrameMatrix, false);
        }

        @Override
        public void clearFrames(Player player, Integer[][] entityIdMatrix) {
            validateArrayDimensions(wrapperMatrix, entityIdMatrix);

            for (int x = 0; x < entityIdMatrix.length; x++) {
                for (int y = 0; y < entityIdMatrix[x].length; y++) {
                    wrapperMatrix[y][x].getController().clearFrame(player, entityIdMatrix[x][y]);
                }
            }
        }

        @Override
        public void clearFrames(Player player, ItemFrame[][] itemFrameMatrix) {
            validateArrayDimensions(wrapperMatrix, itemFrameMatrix);

            for (int x = 0; x < itemFrameMatrix.length; x++) {
                for (int y = 0; y < itemFrameMatrix[x].length; y++) {
                    wrapperMatrix[y][x].getController().clearFrame(player, itemFrameMatrix[x][y]);
                }
            }
        }
    };

    /*
     * Modified Method from http://kalanir.blogspot.de/2010/02/how-to-split-image-into-chunks-java.html
     */
    private static ArrayImage[][] splitImage(final BufferedImage image, final int columns, final int rows) {
        int chunkWidth = image.getWidth() / columns;
        int chunkHeight = image.getHeight() / rows;

        ArrayImage[][] images = new ArrayImage[rows][columns];
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                BufferedImage raw = new BufferedImage(chunkWidth, chunkHeight, image.getType());

                Graphics2D gr = raw.createGraphics();
                gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                gr.dispose();

                images[x][y] = new ArrayImage(raw);
                raw.flush();
            }
        }
        return images;
    }

    @Override
    public MultiMapController getController() {
        return controller;
    }
}
