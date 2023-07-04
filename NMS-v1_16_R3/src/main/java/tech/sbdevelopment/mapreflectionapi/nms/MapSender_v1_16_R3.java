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

package tech.sbdevelopment.mapreflectionapi.nms;

import net.minecraft.server.v1_16_R3.PacketPlayOutMap;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.api.ArrayImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapSender_v1_16_R3 {
    private static final List<QueuedMap> sendQueue = new ArrayList<>();
    private static int senderID = -1;

    private MapSender_v1_16_R3() {
    }

    public static void addToQueue(final int id, final ArrayImage content, final Player player) {
        QueuedMap toSend = new QueuedMap(id, content, player);
        if (sendQueue.contains(toSend)) return;
        sendQueue.add(toSend);

        runSender();
    }

    /**
     * Cancels a senderID in the sender queue
     *
     * @param s The senderID to cancel
     */
    public static void cancelID(int s) {
        sendQueue.removeIf(queuedMap -> queuedMap.id == s);
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
                        false, //Tracking position
                        false, //Locked
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
            var that = (QueuedMap) obj;
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
