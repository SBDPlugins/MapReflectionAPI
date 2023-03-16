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
import tech.sbdevelopment.mapreflectionapi.api.events.CreateInventoryMapUpdateEvent;
import tech.sbdevelopment.mapreflectionapi.api.events.MapCancelEvent;
import tech.sbdevelopment.mapreflectionapi.api.events.MapInteractEvent;
import tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtil;

import java.util.concurrent.TimeUnit;

import static tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtil.*;

public class PacketListener implements Listener {
    private static final Class<?> packetPlayOutMapClass = getNMSClass("network.protocol.game", "PacketPlayOutMap");
    private static final Class<?> packetPlayInUseEntityClass = getNMSClass("network.protocol.game", "PacketPlayInUseEntity");
    private static final Class<?> packetPlayInSetCreativeSlotClass = getNMSClass("network.protocol.game", "PacketPlayInSetCreativeSlot");
    private static final Class<?> vec3DClass = getNMSClass("world.phys", "Vec3D");
    private static final Class<?> craftStackClass = getCraftClass("inventory.CraftItemStack");

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
                        if (event.getHandlers().getRegisteredListeners().length > 0)
                            Bukkit.getPluginManager().callEvent(event);

                        if (event.isCancelled()) return;
                    }
                }

                super.write(ctx, packet, promise);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
                if (packet.getClass().isAssignableFrom(packetPlayInUseEntityClass)) {
                    Object packetPlayInEntity = packetPlayInUseEntityClass.cast(packet);

                    int entityId = (int) getDeclaredField(packetPlayInEntity, "a");

                    Enum<?> actionEnum;
                    Enum<?> hand;
                    Object pos;
                    if (ReflectionUtil.supports(17)) {
                        Object action = getDeclaredField(packetPlayInEntity, "b");
                        actionEnum = (Enum<?>) callDeclaredMethod(action, "a"); //action type
                        hand = hasField(action, "a") ? (Enum<?>) getDeclaredField(action, "a") : null;
                        pos = hasField(action, "b") ? getDeclaredField(action, "b") : null;
                    } else {
                        actionEnum = (Enum<?>) callDeclaredMethod(packetPlayInEntity, ReflectionUtil.supports(13) ? "b" : "a"); //1.13 = b, 1.12 = a
                        hand = (Enum<?>) callDeclaredMethod(packetPlayInEntity, ReflectionUtil.supports(13) ? "c" : "b"); //1.13 = c, 1.12 = b
                        pos = callDeclaredMethod(packetPlayInEntity, ReflectionUtil.supports(13) ? "d" : "c"); //1.13 = d, 1.12 = c
                    }

                    if (Bukkit.getScheduler().callSyncMethod(MapReflectionAPI.getInstance(), () -> {
                        boolean async = !MapReflectionAPI.getInstance().getServer().isPrimaryThread();
                        MapInteractEvent event = new MapInteractEvent(player, entityId, actionEnum.ordinal(), pos != null ? vec3DToVector(pos) : null, hand != null ? hand.ordinal() : 0, async);
                        if (event.getFrame() != null && event.getMapWrapper() != null) {
                            Bukkit.getPluginManager().callEvent(event);
                            return event.isCancelled();
                        }
                        return false;
                    }).get(1, TimeUnit.SECONDS)) return;
                } else if (packet.getClass().isAssignableFrom(packetPlayInSetCreativeSlotClass)) {
                    Object packetPlayInSetCreativeSlot = packetPlayInSetCreativeSlotClass.cast(packet);

                    int slot = (int) ReflectionUtil.callDeclaredMethod(packetPlayInSetCreativeSlot, ReflectionUtil.supports(19, 3) ? "a" : ReflectionUtil.supports(13) ? "b" : "a"); //1.19.4 = a, 1.19.3 - 1.13 = b, 1.12 = a
                    Object nmsStack = ReflectionUtil.callDeclaredMethod(packetPlayInSetCreativeSlot, ReflectionUtil.supports(18) ? "c" : "getItemStack"); //1.18 = c, 1.17 = getItemStack
                    ItemStack craftStack = (ItemStack) ReflectionUtil.callMethod(craftStackClass, "asBukkitCopy", nmsStack);

                    boolean async = !MapReflectionAPI.getInstance().getServer().isPrimaryThread();
                    CreateInventoryMapUpdateEvent event = new CreateInventoryMapUpdateEvent(player, slot, craftStack, async);
                    if (event.getMapWrapper() != null) {
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) return;
                    }
                }

                super.channelRead(ctx, packet);
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
        Object playerConnection = getDeclaredField(playerHandle, ReflectionUtil.supports(17) ? "b" : "playerConnection"); //1.17 = b, 1.16 = playerConnection
        Object networkManager = getDeclaredField(playerConnection, ReflectionUtil.supports(19, 3) ? "h" : ReflectionUtil.supports(19) ? "b" : ReflectionUtil.supports(17) ? "a" : "networkManager"); //1.19.4 = h, >= 1.19.3 = b, 1.18 = a, 1.16 = networkManager
        return (Channel) getDeclaredField(networkManager, ReflectionUtil.supports(18) ? "m" : ReflectionUtil.supports(17) ? "k" : "channel"); //1.19 & 1.18 = m, 1.17 = k, 1.16 = channel
    }

    private Vector vec3DToVector(Object vec3d) {
        if (!(vec3d.getClass().isAssignableFrom(vec3DClass))) return new Vector(0, 0, 0);

        Object vec3dNMS = vec3DClass.cast(vec3d);
        double x = (double) ReflectionUtil.getDeclaredField(vec3dNMS, ReflectionUtil.supports(19) ? "c" : ReflectionUtil.supports(17) ? "b" : "x"); //1.19 = c, 1.18 = b, 1.16 = x
        double y = (double) ReflectionUtil.getDeclaredField(vec3dNMS, ReflectionUtil.supports(19) ? "d" : ReflectionUtil.supports(17) ? "c" : "y"); //1.19 = d, 1.18 = c, 1.16 = y
        double z = (double) ReflectionUtil.getDeclaredField(vec3dNMS, ReflectionUtil.supports(19) ? "e" : ReflectionUtil.supports(17) ? "d" : "z"); //1.19 = e, 1.18 = d, 1.16 = z

        return new Vector(x, y, z);
    }
}
