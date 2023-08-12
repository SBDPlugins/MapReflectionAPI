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

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A {@link MultiMapWrapper} wraps one image split in pieces.
 */
public class MultiMapWrapper extends AbstractMapWrapper {
    private final MapWrapper[][] wrapperMatrix;

    /**
     * Creates a new {@link MultiMapWrapper} from the given image.
     * The image will be split into the given amount of rows and columns.
     *
     * @param image   The image to wrap
     * @param rows    The amount of rows
     * @param columns The amount of columns
     */
    public MultiMapWrapper(BufferedImage image, int rows, int columns) {
        this(splitImage(image, rows, columns));
    }

    /**
     * Creates a new {@link MultiMapWrapper} from the given image.
     * The image will be split into the given amount of rows and columns.
     *
     * @param image   The image to wrap
     * @param rows    The amount of rows
     * @param columns The amount of columns
     */
    public MultiMapWrapper(ArrayImage image, int rows, int columns) {
        this(splitImage(image.toBuffered(), rows, columns));
    }

    /**
     * Creates a new {@link MultiMapWrapper} from the given image.
     *
     * @param imageMatrix The image matrix to wrap
     * @deprecated Use {@link #MultiMapWrapper(ArrayImage, int, int)} instead, this method is meant for internal use only.
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public MultiMapWrapper(ArrayImage[][] imageMatrix) {
        wrapperMatrix = new MapWrapper[imageMatrix.length][imageMatrix[0].length];

        for (int row = 0; row < imageMatrix.length; row++) {
            if (imageMatrix[row].length != imageMatrix[0].length) {
                throw new IllegalArgumentException("An image in a MultiMapWrapper is not rectangular!");
            }

            for (int column = 0; column < imageMatrix[row].length; column++) {
                wrapperMatrix[row][column] = MapReflectionAPI.getMapManager().wrapImage(imageMatrix[row][column]);
            }
        }
    }

    /**
     * Creates a new {@link MultiMapWrapper} from the given image.
     *
     * @param imageMatrix The image matrix to wrap
     * @deprecated Use {@link #MultiMapWrapper(BufferedImage, int, int)} instead, this method is meant for internal use only.
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public MultiMapWrapper(BufferedImage[][] imageMatrix) {
        wrapperMatrix = new MapWrapper[imageMatrix.length][imageMatrix[0].length];

        for (int row = 0; row < imageMatrix.length; row++) {
            if (imageMatrix[row].length != imageMatrix[0].length) {
                throw new IllegalArgumentException("An image in a MultiMapWrapper is not rectangular!");
            }

            for (int column = 0; column < imageMatrix[row].length; column++) {
                wrapperMatrix[row][column] = MapReflectionAPI.getMapManager().wrapImage(imageMatrix[row][column]);
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
            ArrayImage[][] split = splitImage(content.toBuffered(), wrapperMatrix.length, wrapperMatrix[0].length);
            for (int row = 0; row < wrapperMatrix.length; row++) {
                for (int column = 0; column < wrapperMatrix[row].length; column++) {
                    wrapperMatrix[row][column].getController().update(split[row][column]);
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
            for (int row = 0; row < entityIdMatrix.length; row++) {
                for (int column = 0; column < entityIdMatrix[row].length; column++) {
                    wrapperMatrix[row][column].getController().showInFrame(player, entityIdMatrix[row][column]);
                }
            }
        }

        @Override
        public void showInFrames(Player player, Integer[][] entityIdMatrix, DebugCallable callable) {
            for (int row = 0; row < entityIdMatrix.length; row++) {
                for (int column = 0; column < entityIdMatrix[row].length; column++) {
                    wrapperMatrix[row][column].getController().showInFrame(player, entityIdMatrix[row][column], callable.call(wrapperMatrix[row][column].getController(), row, column));
                }
            }
        }

        @Override
        public void showInFrames(Player player, ItemFrame[][] itemFrameMatrix, boolean force) {
            for (int row = 0; row < itemFrameMatrix.length; row++) {
                for (int column = 0; column < itemFrameMatrix[row].length; column++) {
                    wrapperMatrix[row][column].getController().showInFrame(player, itemFrameMatrix[row][column], force);
                }
            }
        }

        @Override
        public void showInFrames(Player player, ItemFrame[][] itemFrameMatrix) {
            showInFrames(player, itemFrameMatrix, false);
        }

        @Override
        public void clearFrames(Player player, Integer[][] entityIdMatrix) {
            for (int row = 0; row < entityIdMatrix.length; row++) {
                for (int column = 0; column < entityIdMatrix[row].length; column++) {
                    wrapperMatrix[row][column].getController().clearFrame(player, entityIdMatrix[row][column]);
                }
            }
        }

        @Override
        public void clearFrames(Player player, ItemFrame[][] itemFrameMatrix) {
            for (int row = 0; row < itemFrameMatrix.length; row++) {
                for (int column = 0; column < itemFrameMatrix[row].length; column++) {
                    wrapperMatrix[row][column].getController().clearFrame(player, itemFrameMatrix[row][column]);
                }
            }
        }
    };

    /**
     * Splits a BufferedImage into a matrix of ArrayImages.
     *
     * @param image   The image to split
     * @param rows    The number of rows
     * @param columns The number of columns
     * @return The matrix of ArrayImages
     */
    private static ArrayImage[][] splitImage(final BufferedImage image, final int rows, final int columns) {
        int chunkWidth = image.getWidth() / columns;
        int chunkHeight = image.getHeight() / rows;

        ArrayImage[][] images = new ArrayImage[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int x = j * chunkWidth;
                int y = i * chunkHeight;

                BufferedImage raw = image.getSubimage(x, y, chunkWidth, chunkHeight);
                images[i][j] = new ArrayImage(raw);
            }
        }

        return images;
    }

    @Override
    public MultiMapController getController() {
        return controller;
    }
}
