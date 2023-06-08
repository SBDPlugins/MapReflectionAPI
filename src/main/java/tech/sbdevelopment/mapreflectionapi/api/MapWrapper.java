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

package tech.sbdevelopment.mapreflectionapi.api;

import org.bukkit.*;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import tech.sbdevelopment.mapreflectionapi.MapReflectionAPI;
import tech.sbdevelopment.mapreflectionapi.api.events.MapContentUpdateEvent;
import tech.sbdevelopment.mapreflectionapi.api.exceptions.MapLimitExceededException;
import tech.sbdevelopment.mapreflectionapi.managers.Configuration;
import tech.sbdevelopment.mapreflectionapi.utils.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A {@link MapWrapper} wraps one image.
 */
public class MapWrapper extends AbstractMapWrapper {
    private static final String REFERENCE_METADATA = "MAP_WRAPPER_REF";
    protected ArrayImage content;

    private static final Material MAP_MATERIAL;

    static {
        MAP_MATERIAL = ReflectionUtil.supports(13) ? Material.FILLED_MAP : Material.MAP;
    }

    /**
     * Construct a new {@link MapWrapper}
     *
     * @param image The {@link ArrayImage} to wrap
     */
    public MapWrapper(ArrayImage image) {
        this.content = image;
    }

    private static final Class<?> craftStackClass = ReflectionUtil.getCraftClass("inventory.CraftItemStack");
    private static final Class<?> setSlotPacketClass = ReflectionUtil.getNMSClass("network.protocol.game", "PacketPlayOutSetSlot");
    private static final Class<?> entityClass = ReflectionUtil.getNMSClass("world.entity", "Entity");
    private static final Class<?> dataWatcherClass = ReflectionUtil.getNMSClass("network.syncher", "DataWatcher");
    private static final Class<?> entityMetadataPacketClass = ReflectionUtil.getNMSClass("network.protocol.game", "PacketPlayOutEntityMetadata");
    private static final Class<?> entityItemFrameClass = ReflectionUtil.getNMSClass("world.entity.decoration", "EntityItemFrame");
    private static final Class<?> dataWatcherItemClass = ReflectionUtil.getNMSClass("network.syncher", "DataWatcher$Item");

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
        public void update(@NotNull ArrayImage content) {
            MapContentUpdateEvent event = new MapContentUpdateEvent(MapWrapper.this, content);
            Bukkit.getPluginManager().callEvent(event);

            if (Configuration.getInstance().isImageCache()) {
                MapWrapper duplicate = MapReflectionAPI.getMapManager().getDuplicate(content);
                if (duplicate != null) {
                    MapWrapper.this.content = duplicate.getContent();
                    return;
                }
            }

            MapWrapper.this.content = content;

            if (event.isSendContent()) {
                for (UUID id : viewers.keySet()) {
                    sendContent(Bukkit.getPlayer(id));
                }
            }
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
        public void cancelSend() {
            for (int s : viewers.values()) {
                MapSender.cancelID(s);
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

            String inventoryMenuName;
            if (ReflectionUtil.supports(20)) { //1.20
                inventoryMenuName = "bQ";
            } else if (ReflectionUtil.supports(19)) { //1.19
                inventoryMenuName = ReflectionUtil.VER_MINOR == 3 ? "bO" : "bT"; //1.19.4 = bO, >= 1.19.3 = bT
            } else if (ReflectionUtil.supports(18)) { //1.18
                inventoryMenuName = ReflectionUtil.VER_MINOR == 1 ? "bV" : "bU"; //1.18.1 = ap, 1.18(.2) = ao
            } else if (ReflectionUtil.supports(17)) { //1.17, same as 1.18(.2)
                inventoryMenuName = "bU";
            } else { //1.12-1.16
                inventoryMenuName = "defaultContainer";
            }
            Object inventoryMenu = ReflectionUtil.getField(ReflectionUtil.getHandle(player), inventoryMenuName);

            ItemStack stack;
            if (ReflectionUtil.supports(13)) {
                stack = new ItemStack(MAP_MATERIAL, 1);
            } else {
                stack = new ItemStack(MAP_MATERIAL, 1, (short) getMapId(player));
            }

            Object nmsStack = createCraftItemStack(stack, (short) getMapId(player));

            Object packet;
            if (ReflectionUtil.supports(17)) { //1.17+
                int stateId = (int) ReflectionUtil.callMethod(inventoryMenu, ReflectionUtil.supports(18) ? "j" : "getStateId");

                packet = ReflectionUtil.callConstructor(setSlotPacketClass,
                        0, //0 = Player inventory
                        stateId,
                        slot,
                        nmsStack
                );
            } else { //1.16-
                packet = ReflectionUtil.callConstructor(setSlotPacketClass,
                        0, //0 = Player inventory
                        slot,
                        nmsStack
                );
            }

            ReflectionUtil.sendPacketSync(player, packet);
        }

        @Override
        public void showInInventory(Player player, int slot) {
            showInInventory(player, slot, false);
        }

        @Override
        public void showInHand(Player player, boolean force) {
            if (player.getInventory().getItemInMainHand().getType() != MAP_MATERIAL && !force)
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
            if (frame.getItem().getType() != MAP_MATERIAL && !force)
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

            ItemStack stack = new ItemStack(MAP_MATERIAL, 1);
            if (debugInfo != null) {
                ItemMeta itemMeta = stack.getItemMeta();
                itemMeta.setDisplayName(debugInfo);
                stack.setItemMeta(itemMeta);
            }

            Bukkit.getScheduler().runTask(MapReflectionAPI.getInstance(), () -> {
                ItemFrame frame = getItemFrameById(player.getWorld(), entityId);
                if (frame != null) {
                    frame.removeMetadata(REFERENCE_METADATA, MapReflectionAPI.getInstance());
                    frame.setMetadata(REFERENCE_METADATA, new FixedMetadataValue(MapReflectionAPI.getInstance(), MapWrapper.this));
                }

                sendItemFramePacket(player, entityId, stack, getMapId(player));
            });
        }

        @Override
        public void clearFrame(Player player, int entityId) {
            sendItemFramePacket(player, entityId, null, -1);
            Bukkit.getScheduler().runTask(MapReflectionAPI.getInstance(), () -> {
                ItemFrame frame = getItemFrameById(player.getWorld(), entityId);
                if (frame != null) frame.removeMetadata(REFERENCE_METADATA, MapReflectionAPI.getInstance());
            });
        }

        @Override
        public void clearFrame(Player player, ItemFrame frame) {
            clearFrame(player, frame.getEntityId());
        }

        @Override
        public ItemFrame getItemFrameById(World world, int entityId) {
            Object worldHandle = ReflectionUtil.getHandle(world);
            Object nmsEntity = ReflectionUtil.callMethod(worldHandle, ReflectionUtil.supports(18) ? "a" : "getEntity", entityId);
            if (nmsEntity == null) return null;

            Object craftEntity = ReflectionUtil.callMethod(nmsEntity, "getBukkitEntity");
            if (craftEntity == null) return null;

            Class<?> itemFrameClass = ReflectionUtil.getNMSClass("world.entity.decoration", "EntityItemFrame");
            if (itemFrameClass == null) return null;

            if (craftEntity.getClass().isAssignableFrom(itemFrameClass))
                return (ItemFrame) itemFrameClass.cast(craftEntity);

            return null;
        }

        private Object createCraftItemStack(@NotNull ItemStack stack, int mapId) {
            if (mapId < 0) return null;

            Object nmsStack = ReflectionUtil.callMethod(craftStackClass, "asNMSCopy", stack);

            if (ReflectionUtil.supports(13)) {
                String nbtObjectName;
                if (ReflectionUtil.supports(19)) { //1.19
                    nbtObjectName = "v";
                } else if (ReflectionUtil.supports(18)) { //1.18
                    nbtObjectName = ReflectionUtil.VER_MINOR == 1 ? "t" : "u"; //1.18.1 = t, 1.18(.2) = u
                } else { //1.13 - 1.17
                    nbtObjectName = "getOrCreateTag";
                }
                Object nbtObject = ReflectionUtil.callMethod(nmsStack, nbtObjectName);
                ReflectionUtil.callMethod(nbtObject, ReflectionUtil.supports(18) ? "a" : "setInt", "map", mapId);
            }
            return nmsStack;
        }

        private void sendItemFramePacket(Player player, int entityId, ItemStack stack, int mapId) {
            Object nmsStack = createCraftItemStack(stack, mapId);

            String dataWatcherObjectName;
            if (ReflectionUtil.supports(19)) { //1.19
                dataWatcherObjectName = ReflectionUtil.VER_MINOR == 3 ? "g" : "ao"; //1.19.4 = g, >= 1.19.3 = ao
            } else if (ReflectionUtil.supports(18)) { //1.18
                dataWatcherObjectName = ReflectionUtil.VER_MINOR == 1 ? "ap" : "ao"; //1.18.1 = ap, 1.18(.2) = ao
            } else if (ReflectionUtil.supports(17)) { //1.17
                dataWatcherObjectName = "ao";
            } else if (ReflectionUtil.supports(14)) { //1.14 - 1.16
                dataWatcherObjectName = "ITEM";
            } else if (ReflectionUtil.supports(13)) { //1.13
                dataWatcherObjectName = "e";
            } else { //1.12
                dataWatcherObjectName = "c";
            }
            Object dataWatcherObject = ReflectionUtil.getDeclaredField(entityItemFrameClass, dataWatcherObjectName);

            ReflectionUtil.ListParam list = new ReflectionUtil.ListParam<>();

            Object packet;
            if (ReflectionUtil.supports(19, 2)) { //1.19.3
                Class<?> dataWatcherRecordClass = ReflectionUtil.getNMSClass("network.syncher", "DataWatcher$b");
                // Sadly not possible to use ReflectionUtil (in its current state), because of the Object parameter
                Object dataWatcherItem;
                try {
                    Method m = dataWatcherRecordClass.getMethod("a", dataWatcherObject.getClass(), Object.class);
                    m.setAccessible(true);
                    dataWatcherItem = m.invoke(null, dataWatcherObject, nmsStack);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                    ex.printStackTrace();
                    return;
                }
                list.add(dataWatcherItem);

                packet = ReflectionUtil.callConstructor(entityMetadataPacketClass,
                        entityId,
                        list
                );
            } else { //1.19.2 or lower
                Object dataWatcher = ReflectionUtil.callConstructorNull(dataWatcherClass, entityClass);

                packet = ReflectionUtil.callConstructor(entityMetadataPacketClass,
                        entityId,
                        dataWatcher, //dummy watcher!
                        true
                );

                Object dataWatcherItem = ReflectionUtil.callFirstConstructor(dataWatcherItemClass, dataWatcherObject, nmsStack);
                list.add(dataWatcherItem);
                ReflectionUtil.setDeclaredField(packet, "b", list);
            }

            ReflectionUtil.sendPacketSync(player, packet);
        }
    };

    public ArrayImage getContent() {
        return content;
    }

    @Override
    public MapController getController() {
        return controller;
    }
}
