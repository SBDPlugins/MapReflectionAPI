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
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.game.PacketPlayInSetCreativeSlot;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.world.EnumHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.api.events.CreateInventoryMapUpdateEvent;
import tech.sbdevelopment.mapreflectionapi.api.events.MapCancelEvent;
import tech.sbdevelopment.mapreflectionapi.api.events.MapInteractEvent;
import tech.sbdevelopment.mapreflectionapi.listeners.PacketListener;

import java.util.concurrent.TimeUnit;

import static tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtil.*;

public class PacketListener_v1_20_R1 extends PacketListener {
    @Override
    protected void injectPlayer(Player p) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            //On send packet
            public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
                if (packet instanceof PacketPlayOutMap packetPlayOutMap) {
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
                if (packet instanceof PacketPlayInUseEntity packetPlayInUseEntity) {
                    int entityId = (int) getDeclaredField(packetPlayInUseEntity, "a"); //entityId
                    Object action = getDeclaredField(packetPlayInUseEntity, "b"); //action
                    Enum<?> actionEnum = (Enum<?>) callDeclaredMethod(action, "a"); //action type
                    EnumHand hand = hasField(action, "a") ? (EnumHand) getDeclaredField(action, "a") : null; //hand
                    Vec3D pos = hasField(action, "b") ? (Vec3D) getDeclaredField(action, "b") : null; //pos

                    if (Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                        boolean async = !plugin.getServer().isPrimaryThread();
                        MapInteractEvent event = new MapInteractEvent(p, entityId, actionEnum.ordinal(), pos != null ? vec3DToVector(pos) : null, hand != null ? hand.ordinal() : 0, async);
                        if (event.getFrame() != null && event.getMapWrapper() != null) {
                            Bukkit.getPluginManager().callEvent(event);
                            return event.isCancelled();
                        }
                        return false;
                    }).get(1, TimeUnit.SECONDS)) return;
                } else if (packet instanceof PacketPlayInSetCreativeSlot packetPlayInSetCreativeSlot) {
                    int slot = packetPlayInSetCreativeSlot.a();
                    ItemStack item = packetPlayInSetCreativeSlot.c();

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

        //The connection is private since 1.19.4 :|
        NetworkManager networkManager = (NetworkManager) getField(((CraftPlayer) p).getHandle().c, "h");
        ChannelPipeline pipeline = networkManager.m.pipeline(); //connection channel
        pipeline.addBefore("packet_handler", p.getName(), channelDuplexHandler);
    }

    @Override
    public void removePlayer(Player p) {
        //The connection is private since 1.19.4 :|
        NetworkManager networkManager = (NetworkManager) getField(((CraftPlayer) p).getHandle().c, "h");
        Channel channel = networkManager.m; //connection channel
        channel.eventLoop().submit(() -> channel.pipeline().remove(p.getName()));
    }

    @Override
    protected Vector vec3DToVector(Object vec3d) {
        if (!(vec3d instanceof Vec3D vec3dObj)) return new Vector(0, 0, 0);
        return new Vector(vec3dObj.c, vec3dObj.d, vec3dObj.e); //x, y, z
    }
}
