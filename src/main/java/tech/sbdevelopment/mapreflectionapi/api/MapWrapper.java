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

package tech.sbdevelopment.mapreflectionapi.api;

import org.bukkit.*;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.exceptions.MapLimitExceededException;
import tech.sbdevelopment.mapreflectionapi.util.ReflectionUtil;
import tech.sbdevelopment.mapreflectionapi.util.ReflectionUtils;

import java.util.*;

public class MapWrapper {
    protected ArrayImage content;

    public MapWrapper(ArrayImage image) {
        this.content = image;
    }

    private static final Class<?> craftStackClass = ReflectionUtils.getCraftClass("CraftItemStack");
    private static final Class<?> setSlotPacketClass = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutSetSlot");
    private static final Class<?> tagCompoundClass = ReflectionUtils.getCraftClass("NBTTagCompound");
    private static final Class<?> entityClass = ReflectionUtils.getNMSClass("world.entity", "Entity");
    private static final Class<?> dataWatcherClass = ReflectionUtils.getNMSClass("network.syncher", "DataWatcher");
    private static final Class<?> entityMetadataPacketClass = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntityMetadata");
    private static final Class<?> entityItemFrameClass = ReflectionUtils.getNMSClass("world.entity.decoration", "EntityItemFrame");
    private static final Class<?> dataWatcherItemClass = ReflectionUtils.getNMSClass("network.syncher", "DataWatcher$Item");

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
                MapWrapper.this.content = duplicate.getContent();
                return;
            }

            MapWrapper.this.content = content;

            for (UUID id : viewers.keySet()) {
                sendContent(Bukkit.getPlayer(id));
            }
        }

        @Override
        public ArrayImage getContent() {
            return MapWrapper.this.getContent();
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
                MapSender.sendMap(id, MapWrapper.this.content, player);
            } else {
                MapSender.addToQueue(id, MapWrapper.this.content, player);
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

            Object playerHandle = ReflectionUtils.getHandle(player);
            Object inventoryMenu = ReflectionUtil.getField(playerHandle, ReflectionUtils.supports(19) ? "bT" : ReflectionUtils.supports(17) ? "bU" : "defaultContainer");
            int windowId = (int) ReflectionUtil.getField(inventoryMenu, ReflectionUtils.supports(17) ? "j" : "windowId");

            ItemStack stack = new ItemStack(ReflectionUtils.supports(13) ? Material.FILLED_MAP : Material.MAP, 1);

            Object nmsStack = ReflectionUtil.callMethod(craftStackClass, "asNMSCopy", stack);

            Object packet;
            if (ReflectionUtils.supports(17)) { //1.17+
                int stateId = (int) ReflectionUtil.callMethod(inventoryMenu, ReflectionUtils.supports(18) ? "j" : "getStateId");

                packet = ReflectionUtil.callConstructor(setSlotPacketClass,
                        windowId,
                        stateId,
                        slot,
                        nmsStack
                );
            } else { //1.16-
                packet = ReflectionUtil.callConstructor(setSlotPacketClass,
                        windowId,
                        slot,
                        nmsStack
                );
            }

            ReflectionUtils.sendPacket(player, packet);
        }

        @Override
        public void showInInventory(Player player, int slot) {
            showInInventory(player, slot, false);
        }

        @Override
        public void showInHand(Player player, boolean force) {
            if (player.getInventory().getItemInMainHand().getType() != (ReflectionUtils.supports(13) ? Material.FILLED_MAP : Material.MAP) && !force)
                return;
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
            if (frame.getItem().getType() != (ReflectionUtils.supports(13) ? Material.FILLED_MAP : Material.MAP) && !force)
                return;
            showInFrame(player, frame.getEntityId());
        }

        @Override
        public void showInFrame(Player player, int entityId) {
            showInFrame(player, entityId, null);
        }

        @Override
        public void showInFrame(Player player, int entityId, String debugInfo) {
            if (!isViewing(player)) return;

            ItemStack stack = new ItemStack(ReflectionUtils.supports(13) ? Material.FILLED_MAP : Material.MAP, 1);
            if (debugInfo != null) {
                ItemMeta itemMeta = stack.getItemMeta();
                itemMeta.setDisplayName(debugInfo);
                stack.setItemMeta(itemMeta);
            }

            Bukkit.getScheduler().runTask(MapReflectionAPI.getInstance(), () -> {
                ItemFrame frame = getItemFrameById(player.getWorld(), entityId);
                if (frame != null) {
                    frame.removeMetadata("MAP_WRAPPER_REF", MapReflectionAPI.getInstance());
                    frame.setMetadata("MAP_WRAPPER_REF", new FixedMetadataValue(MapReflectionAPI.getInstance(), MapWrapper.this));
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
            Object worldHandle = ReflectionUtils.getHandle(world);
            Object nmsEntity = ReflectionUtil.callMethod(worldHandle, ReflectionUtils.supports(18) ? "a" : "getEntity");
            if (nmsEntity == null) return null;

            if (!ReflectionUtils.supports(17)) {
                nmsEntity = ReflectionUtil.callMethod(nmsEntity, "getBukkitEntity");
            }

            if (nmsEntity instanceof ItemFrame) return (ItemFrame) nmsEntity;
            return null;
        }

        private void sendItemFramePacket(Player player, int entityId, ItemStack stack, int mapId) {
            Object nmsStack = ReflectionUtil.callMethod(craftStackClass, "asNMSCopy", stack);
            Object nbtObject = ReflectionUtil.callMethod(nmsStack, ReflectionUtils.supports(19) ? "v" : ReflectionUtils.supports(18) ? "u" : ReflectionUtils.supports(13) ? "getOrCreateTag" : "getTag");

            if (!ReflectionUtils.supports(13) && nbtObject == null) { //1.12 has no getOrCreate, call create if null!
                Object tagCompound = ReflectionUtil.callConstructor(tagCompoundClass);
                ReflectionUtil.callMethod(nbtObject, "setTag", tagCompound);
            }

            ReflectionUtil.callMethod(nbtObject, ReflectionUtils.supports(18) ? "a" : "setInt", "map", mapId);
            Object dataWatcher = ReflectionUtil.callConstructor(dataWatcherClass, entityClass.cast(null));

            Object packet = ReflectionUtil.callConstructor(entityMetadataPacketClass,
                    entityId,
                    dataWatcher, //dummy watcher!
                    true
            );

            List<Object> list = new ArrayList<>();
            Object dataWatcherObject = ReflectionUtil.getDeclaredField(entityItemFrameClass, ReflectionUtils.supports(17) ? "ao" : ReflectionUtils.supports(14) ? "ITEM" : ReflectionUtils.supports(13) ? "e" : "c");
            Object dataWatcherItem = ReflectionUtil.callConstructor(dataWatcherItemClass, dataWatcherObject, nmsStack);
            list.add(dataWatcherItem);
            ReflectionUtil.setDeclaredField(packet, "b", list);

            ReflectionUtils.sendPacket(player, packet);
        }
    };

    public ArrayImage getContent() {
        return content;
    }

    public MapController getController() {
        return controller;
    }


}
