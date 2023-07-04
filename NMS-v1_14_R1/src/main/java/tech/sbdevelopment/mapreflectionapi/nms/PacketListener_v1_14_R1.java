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

import io.netty.channel.*;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.api.events.CreateInventoryMapUpdateEvent;
import tech.sbdevelopment.mapreflectionapi.api.events.MapCancelEvent;
import tech.sbdevelopment.mapreflectionapi.api.events.MapInteractEvent;
import tech.sbdevelopment.mapreflectionapi.listeners.PacketListener;

import java.util.concurrent.TimeUnit;

import static tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtil.getDeclaredField;
import static tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtil.setDeclaredField;

public class PacketListener_v1_14_R1 extends PacketListener {
    @Override
    protected void injectPlayer(Player p) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            //On send packet
            public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
                if (packet instanceof PacketPlayOutMap) {
                    PacketPlayOutMap packetPlayOutMap = (PacketPlayOutMap) packet;

                    int id = (int) getDeclaredField(packetPlayOutMap, "a");
                    if (id < 0) {
                        //It's one of our maps, invert ID and let through!
                        int newId = -id;
                        setDeclaredField(packetPlayOutMap, "a", newId); //mapId
                    } else {
                        boolean async = !plugin.getServer().isPrimaryThread();
                        MapCancelEvent event = new MapCancelEvent(p, id, async);
                        if (MapReflectionAPI.getMapManager().isIdUsedBy(p, id)) event.setCancelled(true);
                        if (event.getHandlers().getRegisteredListeners().length > 0)
                            Bukkit.getPluginManager().callEvent(event);

                        if (event.isCancelled()) return;
                    }
                }

                super.write(ctx, packet, promise);
            }

            @Override
            //On receive packet
            public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
                if (packet instanceof PacketPlayInUseEntity) {
                    PacketPlayInUseEntity packetPlayInUseEntity = (PacketPlayInUseEntity) packet;

                    int entityId = (int) getDeclaredField(packetPlayInUseEntity, "a"); //entityId
                    PacketPlayInUseEntity.EnumEntityUseAction action = packetPlayInUseEntity.b(); //action
                    EnumHand hand = packetPlayInUseEntity.c(); //hand
                    Vec3D pos = packetPlayInUseEntity.d(); //pos

                    if (Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                        boolean async = !plugin.getServer().isPrimaryThread();
                        MapInteractEvent event = new MapInteractEvent(p, entityId, action.ordinal(), pos != null ? vec3DToVector(pos) : null, hand != null ? hand.ordinal() : 0, async);
                        if (event.getFrame() != null && event.getMapWrapper() != null) {
                            Bukkit.getPluginManager().callEvent(event);
                            return event.isCancelled();
                        }
                        return false;
                    }).get(1, TimeUnit.SECONDS)) return;
                } else if (packet instanceof PacketPlayInSetCreativeSlot) {
                    PacketPlayInSetCreativeSlot packetPlayInSetCreativeSlot = (PacketPlayInSetCreativeSlot) packet;

                    int slot = packetPlayInSetCreativeSlot.b();
                    ItemStack item = packetPlayInSetCreativeSlot.getItemStack();

                    boolean async = !plugin.getServer().isPrimaryThread();
                    CreateInventoryMapUpdateEvent event = new CreateInventoryMapUpdateEvent(p, slot, CraftItemStack.asBukkitCopy(item), async);
                    if (event.getMapWrapper() != null) {
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) return;
                    }
                }

                super.channelRead(ctx, packet);
            }
        };

        ChannelPipeline pipeline = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler", p.getName(), channelDuplexHandler);
    }

    @Override
    public void removePlayer(Player p) {
        Channel channel = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> channel.pipeline().remove(p.getName()));
    }

    @Override
    protected Vector vec3DToVector(Object vec3d) {
        if (!(vec3d instanceof Vec3D)) return new Vector(0, 0, 0);

        Vec3D vec3dObj = (Vec3D) vec3d;
        return new Vector(vec3dObj.x, vec3dObj.y, vec3dObj.z);
    }
}
