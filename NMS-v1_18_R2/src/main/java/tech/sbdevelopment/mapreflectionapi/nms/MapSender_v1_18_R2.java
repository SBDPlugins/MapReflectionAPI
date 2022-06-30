package tech.sbdevelopment.mapreflectionapi.nms;

import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import tech.sbdevelopment.mapreflectionapi.ArrayImage;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;

import java.util.ArrayList;
import java.util.List;

public class MapSender_v1_18_R2 {
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
                WorldMap.b updateData = new WorldMap.b(
                        content.minX, //X pos
                        content.minY, //Y pos
                        content.maxX, //X size (2nd X pos)
                        content.maxY, //Y size (2nd Y pos)
                        content.array //Data
                );

                PacketPlayOutMap packet = new PacketPlayOutMap(
                        id, //ID
                        (byte) 0, //Scale
                        false, //Show icons
                        new ArrayList<>(), //Icons
                        updateData
                );

                ((CraftPlayer) player).getHandle().connection.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    record QueuedMap(int id, ArrayImage image, Player player) {
    }
}
