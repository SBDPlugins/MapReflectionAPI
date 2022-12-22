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

package tech.sbdevelopment.mapreflectionapi.api;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link MapSender} sends the Map packets to players.
 */
public class MapSender {
    private static final List<QueuedMap> sendQueue = new ArrayList<>();
    private static int senderID = -1;

    private MapSender() {
    }

    /**
     * Add a map to the send queue
     *
     * @param id      The ID of the map
     * @param content The {@link ArrayImage} to view on the map
     * @param player  The {@link Player} to view for
     */
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

    /**
     * Run the sender task
     */
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

    private static final Class<?> packetPlayOutMapClass = ReflectionUtil.getNMSClass("network.protocol.game", "PacketPlayOutMap");
    private static final Class<?> worldMapData = ReflectionUtil.supports(17) ? ReflectionUtil.getNMSClass("world.level.saveddata.maps", "WorldMap$b") : null;

    /**
     * Send a map to a player
     *
     * @param id0     The ID of the map
     * @param content The {@link ArrayImage} to view on the map
     * @param player  The {@link Player} to view for
     */
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
        Object packet;
        if (ReflectionUtil.supports(17)) { //1.17+
            Object updateData = ReflectionUtil.callConstructor(worldMapData,
                    content.minX, //X pos
                    content.minY, //Y pos
                    content.maxX, //X size (2nd X pos)
                    content.maxY, //Y size (2nd Y pos)
                    content.array //Data
            );

            packet = ReflectionUtil.callConstructor(packetPlayOutMapClass,
                    id, //ID
                    (byte) 0, //Scale, 0 = 1 block per pixel
                    false, //Show icons
                    new ReflectionUtil.CollectionParam<>(), //Icons
                    updateData
            );
        } else if (ReflectionUtil.supports(14)) { //1.16-1.14
            packet = ReflectionUtil.callConstructor(packetPlayOutMapClass,
                    id, //ID
                    (byte) 0, //Scale, 0 = 1 block per pixel
                    false, //Tracking position
                    false, //Locked
                    new ReflectionUtil.CollectionParam<>(), //Icons
                    content.array, //Data
                    content.minX, //X pos
                    content.minY, //Y pos
                    content.maxX, //X size (2nd X pos)
                    content.maxY //Y size (2nd Y pos)
            );
        } else { //1.13-
            packet = ReflectionUtil.callConstructor(packetPlayOutMapClass,
                    id, //ID
                    (byte) 0, //Scale, 0 = 1 block per pixel
                    false, //???
                    new ReflectionUtil.CollectionParam<>(), //Icons
                    content.array, //Data
                    content.minX, //X pos
                    content.minY, //Y pos
                    content.maxX, //X size (2nd X pos)
                    content.maxY //Y size (2nd Y pos)
            );
        }

        ReflectionUtil.sendPacket(player, packet);
    }

    @Data
    static final class QueuedMap {
        private final int id;
        private final ArrayImage image;
        private final Player player;
    }
}
