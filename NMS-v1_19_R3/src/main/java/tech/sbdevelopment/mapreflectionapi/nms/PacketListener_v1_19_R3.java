/*
 * This file is part of MapReflectionAPI.
 * Copyright (c) 2022-2023 inventivetalent / SBDevelopment - All Rights Reserved
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

import io.netty.channel.*;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.game.PacketPlayInSetCreativeSlot;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.world.EnumHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.api.events.CreateInventoryMapUpdateEvent;
import tech.sbdevelopment.mapreflectionapi.api.events.MapCancelEvent;
import tech.sbdevelopment.mapreflectionapi.api.events.MapInteractEvent;
import tech.sbdevelopment.mapreflectionapi.listeners.PacketListener;

import java.util.concurrent.TimeUnit;

import static tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtil.*;

public class PacketListener_v1_19_R3 extends PacketListener {
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
        NetworkManager networkManager = (NetworkManager) getField(((CraftPlayer) p).getHandle().b, "h");
        ChannelPipeline pipeline = networkManager.m.pipeline(); //connection channel
        pipeline.addBefore("packet_handler", p.getName(), channelDuplexHandler);
    }

    @Override
    public void removePlayer(Player p) {
        //The connection is private since 1.19.4 :|
        NetworkManager networkManager = (NetworkManager) getField(((CraftPlayer) p).getHandle().b, "h");
        Channel channel = networkManager.m; //connection channel
        channel.eventLoop().submit(() -> channel.pipeline().remove(p.getName()));
    }

    @Override
    protected Vector vec3DToVector(Object vec3d) {
        if (!(vec3d instanceof Vec3D vec3dObj)) return new Vector(0, 0, 0);
        return new Vector(vec3dObj.c, vec3dObj.d, vec3dObj.e); //x, y, z
    }
}
