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

package tech.sbdevelopment.mapreflectionapi.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.api.events.CreateInventoryMapUpdateEvent;
import tech.sbdevelopment.mapreflectionapi.api.events.MapCancelEvent;
import tech.sbdevelopment.mapreflectionapi.api.events.MapInteractEvent;

public class PacketListener extends PacketAdapter {
    public PacketListener(Plugin plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.MAP, PacketType.Play.Client.USE_ENTITY, PacketType.Play.Client.SET_CREATIVE_SLOT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.MAP) {
            handleOUTMapPacket(event);
        }
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
            handleINUseEntityPacket(event);
        } else if (event.getPacketType() == PacketType.Play.Client.SET_CREATIVE_SLOT) {
            handleINSetCreativeSlotPacket(event);
        }
    }

    private void handleOUTMapPacket(PacketEvent event) {
        int id = event.getPacket().getIntegers().read(0); //Read first int (a); that's the MAP id

        if (id < 0) {
            //It's one of our maps, invert ID and let through!
            int newId = -id;
            event.getPacket().getIntegers().write(0, newId); //set the MAP id to the reverse
        } else {
            boolean async = !plugin.getServer().isPrimaryThread();
            MapCancelEvent cancelEvent = new MapCancelEvent(event.getPlayer(), id, async);
            if (MapReflectionAPI.getMapManager().isIdUsedBy(event.getPlayer(), id)) cancelEvent.setCancelled(true);
            if (cancelEvent.getHandlers().getRegisteredListeners().length > 0)
                Bukkit.getPluginManager().callEvent(cancelEvent);

            if (cancelEvent.isCancelled()) event.setCancelled(true);
        }
    }

    private void handleINUseEntityPacket(PacketEvent event) {
        int entityId = event.getPacket().getIntegers().read(0); //entityId
        WrappedEnumEntityUseAction action = event.getPacket().getEnumEntityUseActions().read(0);
        EnumWrappers.EntityUseAction actionEnum = null;
        EnumWrappers.Hand hand = null;
        Vector pos = null;
        try {
            actionEnum = action.getAction();
            hand = action.getHand();
            pos = action.getPosition();
        } catch (IllegalArgumentException ignored) {
        }

        boolean async = !plugin.getServer().isPrimaryThread();
        MapInteractEvent interactEvent = new MapInteractEvent(event.getPlayer(), entityId, actionEnum != null ? actionEnum.ordinal() : 0, pos, hand != null ? hand.ordinal() : 0, async);
        if (interactEvent.getFrame() != null && interactEvent.getMapWrapper() != null) {
            Bukkit.getPluginManager().callEvent(interactEvent);
            if (interactEvent.isCancelled()) event.setCancelled(true);
        }
    }

    private void handleINSetCreativeSlotPacket(PacketEvent event) {
        int slot = event.getPacket().getIntegers().read(0);
        ItemStack item = event.getPacket().getItemModifier().read(0);

        boolean async = !plugin.getServer().isPrimaryThread();
        CreateInventoryMapUpdateEvent updateEvent = new CreateInventoryMapUpdateEvent(event.getPlayer(), slot, item, async);
        if (updateEvent.getMapWrapper() != null) {
            Bukkit.getPluginManager().callEvent(updateEvent);
            if (updateEvent.isCancelled()) event.setCancelled(true);
        }
    }
}
