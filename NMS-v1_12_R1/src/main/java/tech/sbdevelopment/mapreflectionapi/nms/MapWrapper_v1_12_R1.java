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

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.api.ArrayImage;
import tech.sbdevelopment.mapreflectionapi.api.MapController;
import tech.sbdevelopment.mapreflectionapi.api.MapWrapper;
import tech.sbdevelopment.mapreflectionapi.exceptions.MapLimitExceededException;

import java.util.*;

import static tech.sbdevelopment.mapreflectionapi.util.ReflectionUtil.getField;
import static tech.sbdevelopment.mapreflectionapi.util.ReflectionUtil.setField;

public class MapWrapper_v1_12_R1 extends MapWrapper {
    protected MapController controller = new MapController() {
        private final Map<UUID, Integer> viewers = new HashMap<>();

        @Override
        public void addViewer(Player player) throws MapLimitExceededException {
            if (!isViewing(player)) {
                viewers.put(player.getUniqueId(), MapReflectionAPI.getMapManager().getNextFreeIdFor(player));
            }
        }

        @Override
        public void removeViewer(OfflinePlayer player) {
            viewers.remove(player.getUniqueId());
        }

        @Override
        public void clearViewers() {
            for (UUID uuid : viewers.keySet()) {
                viewers.remove(uuid);
            }
        }

        @Override
        public boolean isViewing(OfflinePlayer player) {
            if (player == null) return false;
            return viewers.containsKey(player.getUniqueId());
        }

        @Override
        public int getMapId(OfflinePlayer player) {
            if (isViewing(player)) {
                return viewers.get(player.getUniqueId());
            }
            return -1;
        }

        @Override
        public void update(ArrayImage content) {
            MapWrapper duplicate = MapReflectionAPI.getMapManager().getDuplicate(content);
            if (duplicate != null) {
                MapWrapper_v1_12_R1.this.content = duplicate.getContent();
                return;
            }

            MapWrapper_v1_12_R1.this.content = content;

            for (UUID id : viewers.keySet()) {
                sendContent(Bukkit.getPlayer(id));
            }
        }

        @Override
        public ArrayImage getContent() {
            return MapWrapper_v1_12_R1.this.getContent();
        }

        @Override
        public void sendContent(Player player) {
            sendContent(player, false);
        }

        @Override
        public void sendContent(Player player, boolean withoutQueue) {
            if (!isViewing(player)) return;

            int id = getMapId(player);
            if (withoutQueue) {
                MapSender_v1_12_R1.sendMap(id, MapWrapper_v1_12_R1.this.content, player);
            } else {
                MapSender_v1_12_R1.addToQueue(id, MapWrapper_v1_12_R1.this.content, player);
            }
        }

        @Override
        public void showInInventory(Player player, int slot, boolean force) {
            if (!isViewing(player)) return;

            if (player.getGameMode() == GameMode.CREATIVE && !force) return;

            if (slot < 9) {
                slot += 36;
            } else if (slot > 35 && slot != 45) {
                slot = 8 - (slot - 36);
            }

            CraftPlayer craftPlayer = (CraftPlayer) player;
            int windowId = craftPlayer.getHandle().defaultContainer.windowId;

            ItemStack stack = new ItemStack(Material.MAP, 1);
            net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);

            PacketPlayOutSetSlot packet = new PacketPlayOutSetSlot(windowId, slot, nmsStack);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }

        @Override
        public void showInInventory(Player player, int slot) {
            showInInventory(player, slot, false);
        }

        @Override
        public void showInHand(Player player, boolean force) {
            if (player.getInventory().getItemInMainHand().getType() != Material.MAP && !force) return;
            showInInventory(player, player.getInventory().getHeldItemSlot(), force);
        }

        @Override
        public void showInHand(Player player) {
            showInHand(player, false);
        }

        @Override
        public void showInFrame(Player player, ItemFrame frame) {
            showInFrame(player, frame, false);
        }

        @Override
        public void showInFrame(Player player, ItemFrame frame, boolean force) {
            if (frame.getItem().getType() != Material.MAP && !force) return;
            showInFrame(player, frame.getEntityId());
        }

        @Override
        public void showInFrame(Player player, int entityId) {
            showInFrame(player, entityId, null);
        }

        @Override
        public void showInFrame(Player player, int entityId, String debugInfo) {
            if (!isViewing(player)) return;

            ItemStack stack = new ItemStack(Material.MAP, 1);
            if (debugInfo != null) {
                ItemMeta itemMeta = stack.getItemMeta();
                itemMeta.setDisplayName(debugInfo);
                stack.setItemMeta(itemMeta);
            }

            Bukkit.getScheduler().runTask(MapReflectionAPI.getInstance(), () -> {
                ItemFrame frame = getItemFrameById(player.getWorld(), entityId);
                if (frame != null) {
                    frame.removeMetadata("MAP_WRAPPER_REF", MapReflectionAPI.getInstance());
                    frame.setMetadata("MAP_WRAPPER_REF", new FixedMetadataValue(MapReflectionAPI.getInstance(), MapWrapper_v1_12_R1.this));
                }

                sendItemFramePacket(player, entityId, stack, getMapId(player));
            });
        }

        @Override
        public void clearFrame(Player player, int entityId) {

        }

        @Override
        public void clearFrame(Player player, ItemFrame frame) {

        }

        @Override
        public ItemFrame getItemFrameById(World world, int entityId) {
            CraftWorld craftWorld = (CraftWorld) world;

            Entity entity = craftWorld.getHandle().getEntity(entityId);
            if (entity == null) return null;

            org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();
            if (bukkitEntity instanceof ItemFrame) return (ItemFrame) bukkitEntity;

            return null;
        }

        private void sendItemFramePacket(Player player, int entityId, ItemStack stack, int mapId) {
            net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
            if (nmsStack.getTag() == null) nmsStack.setTag(new NBTTagCompound()); //No orCreate on 1.12.2!
            nmsStack.getTag().setInt("map", mapId); //getTag putInt

            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(entityId, new DataWatcher(null), true);

            try {
                List<DataWatcher.Item<?>> list = new ArrayList<>();
                DataWatcherObject<net.minecraft.server.v1_12_R1.ItemStack> dataWatcherObject = (DataWatcherObject<net.minecraft.server.v1_12_R1.ItemStack>) getField(EntityItemFrame.class, "c");
                DataWatcher.Item<?> dataWatcherItem = new DataWatcher.Item<>(dataWatcherObject, nmsStack);
                list.add(dataWatcherItem);
                setField(packet, "b", list);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    };

    public MapWrapper_v1_12_R1(ArrayImage image) {
        super(image);
    }

    @Override
    public MapController getController() {
        return controller;
    }
}
