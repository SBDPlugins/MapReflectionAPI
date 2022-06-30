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

package tech.sbdevelopment.mapreflectionapi.nms;

import net.minecraft.server.v1_12_R1.PacketPlayOutMap;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import tech.sbdevelopment.mapreflectionapi.ArrayImage;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapSender_v1_12_R1 {
    private static final List<QueuedMap> sendQueue = new ArrayList<>();
    private static int senderID = -1;

    public static void addToQueue(final int id, final ArrayImage content, final Player player) {
        QueuedMap toSend = new QueuedMap(id, content, player);
        if (sendQueue.contains(toSend)) return;
        sendQueue.add(toSend);

        runSender();
    }

    private static void runSender() {
        if (Bukkit.getScheduler().isQueued(senderID) || Bukkit.getScheduler().isCurrentlyRunning(senderID) || sendQueue.isEmpty())
            return;

        senderID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MapReflectionAPI.getInstance(), () -> {
            if (sendQueue.isEmpty()) return;

            for (int i = 0; i < Math.min(sendQueue.size(), 10 + 1); i++) {
                QueuedMap current = sendQueue.get(0);
                if (current == null) return;

                sendMap(current.id, current.image, current.player);

                if (!sendQueue.isEmpty()) sendQueue.remove(0);
            }
        }, 0, 2);
    }

    public static void sendMap(final int id0, final ArrayImage content, final Player player) {
        if (player == null || !player.isOnline()) {
            List<QueuedMap> toRemove = new ArrayList<>();
            for (QueuedMap qMap : sendQueue) {
                if (qMap == null) continue;

                if (qMap.player == null || !qMap.player.isOnline()) {
                    toRemove.add(qMap);
                }
            }
            Bukkit.getScheduler().cancelTask(senderID);
            sendQueue.removeAll(toRemove);

            return;
        }

        final int id = -id0;
        Bukkit.getScheduler().runTaskAsynchronously(MapReflectionAPI.getInstance(), () -> {
            try {
                PacketPlayOutMap packet = new PacketPlayOutMap(
                        id, //ID
                        (byte) 0, //Scale
                        false, //???
                        new ArrayList<>(), //Icons
                        content.array, //Data
                        content.minX, //X pos
                        content.minY, //Y pos
                        content.maxX, //X size (2nd X pos)
                        content.maxY //Y size (2nd Y pos)
                );

                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    static final class QueuedMap {
        private final int id;
        private final ArrayImage image;
        private final Player player;

        QueuedMap(int id, ArrayImage image, Player player) {
            this.id = id;
            this.image = image;
            this.player = player;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            QueuedMap that = (QueuedMap) obj;
            return this.id == that.id &&
                    Objects.equals(this.image, that.image) &&
                    Objects.equals(this.player, that.player);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, image, player);
        }

        @Override
        public String toString() {
            return "QueuedMap[" +
                    "id=" + id + ", " +
                    "image=" + image + ", " +
                    "player=" + player + ']';
        }
    }
}
