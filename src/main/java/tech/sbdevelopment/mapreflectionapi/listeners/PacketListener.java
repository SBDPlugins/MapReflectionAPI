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

package tech.sbdevelopment.mapreflectionapi.listeners;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.api.events.CreativeInventoryMapUpdateEvent;
import tech.sbdevelopment.mapreflectionapi.api.events.MapCancelEvent;
import tech.sbdevelopment.mapreflectionapi.api.events.MapInteractEvent;
import tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtil;

import java.util.concurrent.TimeUnit;

import static com.cryptomorin.xseries.reflection.minecraft.MinecraftConnection.getHandle;
import static tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtil.*;
import static com.cryptomorin.xseries.reflection.XReflection.*;

public class PacketListener implements Listener {
    private static final Class<?> packetPlayOutMapClass = getNMSClass("network.protocol.game", "PacketPlayOutMap");
    private static final Class<?> packetPlayInUseEntityClass = getNMSClass("network.protocol.game", "PacketPlayInUseEntity");
    private static final Class<?> packetPlayInSetCreativeSlotClass = getNMSClass("network.protocol.game", "PacketPlayInSetCreativeSlot");
    private static final Class<?> vec3DClass = getNMSClass("world.phys", "Vec3D");
    private static final Class<?> craftStackClass = getCraftClass("inventory.CraftItemStack");
    private static final Class<?> playerCommonConnection;

    static {
        if (supports(20) && supportsPatch(2)) {
            // The packet send method has been abstracted from ServerGamePacketListenerImpl to ServerCommonPacketListenerImpl in 1.20.2
            playerCommonConnection = getNMSClass("server.network", "ServerCommonPacketListenerImpl");
        } else {
            playerCommonConnection = getNMSClass("server.network", "PlayerConnection");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        injectPlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        removePlayer(e.getPlayer());
    }

    private void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
                boolean cancel = false;

                if (packet.getClass().isAssignableFrom(packetPlayOutMapClass)) {
                    Object packetPlayOutMap = packetPlayOutMapClass.cast(packet);

                    int id = (int) getDeclaredField(packetPlayOutMap, "a");
                    if (id < 0) {
                        int newId = -id;
                        setDeclaredField(packetPlayOutMap, "a", newId);
                    } else {
                        boolean async = !MapReflectionAPI.getInstance().getServer().isPrimaryThread();
                        MapCancelEvent event = new MapCancelEvent(player, id, async);
                        if (MapReflectionAPI.getMapManager().isIdUsedBy(player, id)) event.setCancelled(true);
                        Bukkit.getPluginManager().callEvent(event);

                        if (event.isCancelled()) cancel = true;
                    }
                }

                if (!cancel) super.write(ctx, packet, promise);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
                boolean cancel = false;

                if (packet.getClass().isAssignableFrom(packetPlayInUseEntityClass)) {
                    Object packetPlayInEntity = packetPlayInUseEntityClass.cast(packet);

                    int entityId = (int) getDeclaredField(packetPlayInEntity, "a");

                    Enum<?> actionEnum;
                    Enum<?> hand;
                    Object pos;
                    if (supports(17)) {
                        Object action = getDeclaredField(packetPlayInEntity, "b");
                        actionEnum = (Enum<?>) callDeclaredMethod(action, "a");
                        Class<?> d = getNMSClass("network.protocol.game", "PacketPlayInUseEntity$d");
                        Class<?> e = getNMSClass("network.protocol.game", "PacketPlayInUseEntity$e");
                        if (action.getClass().isAssignableFrom(e)) {
                            hand = (Enum<?>) getDeclaredField(action, "a");
                            pos = getDeclaredField(action, "b");
                        } else {
                            pos = null;
                            if (action.getClass().isAssignableFrom(d)) {
                                hand = (Enum<?>) getDeclaredField(action, "a");
                            } else {
                                hand = null;
                            }
                        }
                    } else {
                        actionEnum = (Enum<?>) callDeclaredMethod(packetPlayInEntity, supports(13) ? "b" : "a"); //1.13 = b, 1.12 = a
                        hand = (Enum<?>) callDeclaredMethod(packetPlayInEntity, supports(13) ? "c" : "b"); //1.13 = c, 1.12 = b
                        pos = callDeclaredMethod(packetPlayInEntity, supports(13) ? "d" : "c"); //1.13 = d, 1.12 = c
                    }

                    if (Bukkit.getScheduler().callSyncMethod(MapReflectionAPI.getInstance(), () -> {
                        boolean async = !MapReflectionAPI.getInstance().getServer().isPrimaryThread();
                        MapInteractEvent event = new MapInteractEvent(player, entityId, actionEnum.ordinal(), pos != null ? vec3DToVector(pos) : null, hand != null ? hand.ordinal() : 0, async);
                        if (event.getFrame() != null && event.getMapWrapper() != null) {
                            Bukkit.getPluginManager().callEvent(event);
                            return event.isCancelled();
                        }
                        return false;
                    }).get(1, TimeUnit.SECONDS)) cancel = true;
                } else if (packet.getClass().isAssignableFrom(packetPlayInSetCreativeSlotClass)) {
                    Object packetPlayInSetCreativeSlot = packetPlayInSetCreativeSlotClass.cast(packet);

                    int slot = (int) ReflectionUtil.callDeclaredMethod(packetPlayInSetCreativeSlot, supports(20, 4) ? "b" : supports(19, 4) ? "a" : supports(13) ? "b" : "a"); //1.20.4 - 1.19.4 = a, 1.19.3 - 1.13 and 1.20.5 = b, 1.12 = a
                    Object nmsStack = ReflectionUtil.callDeclaredMethod(packetPlayInSetCreativeSlot, supports(20, 4) ? "e" : supports(20, 2) ? "d" : supports(18) ? "c" : "getItemStack"); //1.20.5 = e, 1.20.2-1.20.4 = d, >= 1.18 = c, 1.17 = getItemStack
                    ItemStack craftStack = (ItemStack) ReflectionUtil.callMethod(craftStackClass, "asBukkitCopy", nmsStack);

                    boolean async = !MapReflectionAPI.getInstance().getServer().isPrimaryThread();
                    CreativeInventoryMapUpdateEvent event = new CreativeInventoryMapUpdateEvent(player, slot, craftStack, async);
                    if (event.getMapWrapper() != null) {
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) cancel = true;
                    }
                }

                if (!cancel) super.channelRead(ctx, packet);
            }
        };

        Channel channel = getChannel(player);
        channel.pipeline().addBefore("packet_handler", player.getName(), channelDuplexHandler);
    }

    private void removePlayer(Player player) {
        Channel channel = getChannel(player);
        channel.eventLoop().submit(() -> channel.pipeline().remove(player.getName()));
    }

    private Channel getChannel(Player player) {
        Object playerHandle = getHandle(player);
        Object playerConnection = getDeclaredField(playerHandle, supports(20) ? "c" : supports(17) ? "b" : "playerConnection"); //1.20 = c, 1.17-1.19 = b, 1.16 = playerConnection
        Object networkManager = getDeclaredField(playerCommonConnection, playerConnection, supports(20, 2) ? "c" : supports(19, 4) ? "h" : supports(19) ? "b" : supports(17) ? "a" : "networkManager"); //1.20.2 = ServerCommonPacketListenerImpl#c, 1.20(.1) & 1.19.4 = h, >= 1.19.3 = b, 1.18 - 1.17 = a, 1.16 = networkManager
        return (Channel) getDeclaredField(networkManager, supports(20, 2) ? "n" : supports(18) ? "m" : supports(17) ? "k" : "channel"); //1.20.2 = n, 1.20(.1), 1.19 & 1.18 = m, 1.17 = k, 1.16 = channel
    }

    private Vector vec3DToVector(Object vec3d) {
        if (!(vec3d.getClass().isAssignableFrom(vec3DClass))) return new Vector(0, 0, 0);

        Object vec3dNMS = vec3DClass.cast(vec3d);
        double x = (double) getDeclaredField(vec3dNMS, supports(19) ? "c" : supports(17) ? "b" : "x"); //1.19 = c, 1.18 = b, 1.16 = x
        double y = (double) getDeclaredField(vec3dNMS, supports(19) ? "d" : supports(17) ? "c" : "y"); //1.19 = d, 1.18 = c, 1.16 = y
        double z = (double) getDeclaredField(vec3dNMS, supports(19) ? "e" : supports(17) ? "d" : "z"); //1.19 = e, 1.18 = d, 1.16 = z

        return new Vector(x, y, z);
    }
}
