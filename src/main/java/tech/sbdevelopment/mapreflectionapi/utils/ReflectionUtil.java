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

package tech.sbdevelopment.mapreflectionapi.utils;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * <b>ReflectionUtil</b> - Reflection handler for NMS and CraftBukkit.<br>
 * Caches the packet related methods and is asynchronous.
 * <p>
 * This class does not handle null checks as most of the requests are from the
 * other utility classes that already handle null checks.
 * <p>
 * <a href="https://wiki.vg/Protocol">Clientbound Packets</a> are considered fake
 * updates to the client without changing the actual data. Since all the data is handled
 * by the server.
 *
 * @author Crypto Morin, Stijn Bannink
 * @version 2.1
 */
public class ReflectionUtil {
    /**
     * We use reflection mainly to avoid writing a new class for version barrier.
     * The version barrier is for NMS that uses the Minecraft version as the main package name.
     * <p>
     * E.g. EntityPlayer in 1.15 is in the class {@code net.minecraft.server.v1_15_R1}
     * but in 1.14 it's in {@code net.minecraft.server.v1_14_R1}
     * In order to maintain cross-version compatibility we cannot import these classes.
     * <p>
     * Performance is not a concern for these specific statically initialized values.
     */
    public static final String VERSION;

    static { // This needs to be right below VERSION because of initialization order.
        // This package loop is used to avoid implementation-dependant strings like Bukkit.getVersion() or Bukkit.getBukkitVersion()
        // which allows easier testing as well.
        String found = null;
        for (Package pack : Package.getPackages()) {
            String name = pack.getName();

            // .v because there are other packages.
            if (name.startsWith("org.bukkit.craftbukkit.v")) {
                found = pack.getName().split("\\.")[3];

                // Just a final guard to make sure it finds this important class.
                // As a protection for forge+bukkit implementation that tend to mix versions.
                // The real CraftPlayer should exist in the package.
                // Note: Doesn't seem to function properly. Will need to separate the version
                // handler for NMS and CraftBukkit for softwares like catmc.
                try {
                    Class.forName("org.bukkit.craftbukkit." + found + ".entity.CraftPlayer");
                    break;
                } catch (ClassNotFoundException e) {
                    found = null;
                }
            }
        }
        if (found == null)
            throw new IllegalArgumentException("Failed to parse server version. Could not find any package starting with name: 'org.bukkit.craftbukkit.v'");
        VERSION = found;
    }

    /**
     * The raw minor version number.
     * E.g. {@code v1_17_R1} to {@code 17}
     *
     * @since 4.0.0
     */
    public static final int VER = Integer.parseInt(VERSION.substring(1).split("_")[1]);
    /**
     * The raw minor version number.
     * E.g. {@code v1_18_R2} to {@code 2}
     *
     * @since 4.0.0
     */
    public static final int VER_MINOR = toInt(VERSION.substring(1).split("_")[2].substring(1), 0);
    /**
     * Mojang remapped their NMS in 1.17 https://www.spigotmc.org/threads/spigot-bungeecord-1-17.510208/#post-4184317
     */
    public static final String
            CRAFTBUKKIT = "org.bukkit.craftbukkit." + VERSION + '.',
            NMS = v(17, "net.minecraft.").orElse("net.minecraft.server." + VERSION + '.');
    /**
     * A nullable public accessible field only available in {@code EntityPlayer}.
     * This can be null if the player is offline.
     */
    private static final MethodHandle PLAYER_CONNECTION;
    /**
     * Responsible for getting the NMS handler {@code EntityPlayer} object for the player.
     * {@code CraftPlayer} is simply a wrapper for {@code EntityPlayer}.
     * Used mainly for handling packet related operations.
     * <p>
     * This is also where the famous player {@code ping} field comes from!
     */
    private static final MethodHandle GET_HANDLE;
    private static final MethodHandle GET_HANDLE_WORLD;
    /**
     * Sends a packet to the player's client through a {@code NetworkManager} which
     * is where {@code ProtocolLib} controls packets by injecting channels!
     */
    private static final MethodHandle SEND_PACKET;

    static {
        Class<?> entityPlayer = getNMSClass("server.level", "EntityPlayer");
        Class<?> worldServer = getNMSClass("server.level", "WorldServer");
        Class<?> craftPlayer = getCraftClass("entity.CraftPlayer");
        Class<?> craftWorld = getCraftClass("CraftWorld");
        Class<?> playerConnection = getNMSClass("server.network", "PlayerConnection");

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle sendPacket = null;
        MethodHandle getHandle = null;
        MethodHandle getHandleWorld = null;
        MethodHandle connection = null;

        try {
            connection = lookup.findGetter(entityPlayer,
                    v(17, "b").orElse("playerConnection"), playerConnection);
            getHandle = lookup.findVirtual(craftPlayer, "getHandle", MethodType.methodType(entityPlayer));
            getHandleWorld = lookup.findVirtual(craftWorld, "getHandle", MethodType.methodType(worldServer));
            sendPacket = lookup.findVirtual(playerConnection,
                    v(18, "a").orElse("sendPacket"),
                    MethodType.methodType(void.class, getNMSClass("network.protocol", "Packet")));
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        PLAYER_CONNECTION = connection;
        SEND_PACKET = sendPacket;
        GET_HANDLE = getHandle;
        GET_HANDLE_WORLD = getHandleWorld;
    }

    private ReflectionUtil() {
    }

    /**
     * This method is purely for readability.
     * No performance is gained.
     *
     * @since 5.0.0
     */
    public static <T> VersionHandler<T> v(int version, T handle) {
        return new VersionHandler<>(version, handle);
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     *
     * @param version the version to compare the server version with.
     * @return true if the version is equal or newer, otherwise false.
     * @since 4.0.0
     */
    public static boolean supports(int version) {
        return VER >= version;
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     *
     * @param major the major version to compare the server version with.
     * @param minor the minor version to compare the server version with.
     * @return true if the version is equal or newer, otherwise false.
     * @since 4.0.0
     */
    public static boolean supports(int major, int minor) {
        return VER >= major && VER_MINOR >= minor;
    }

    private static Class<?> wrapperToPrimitive(Class<?> clazz) {
        if (clazz == Boolean.class) return boolean.class;
        if (clazz == Integer.class) return int.class;
        if (clazz == Double.class) return double.class;
        if (clazz == Float.class) return float.class;
        if (clazz == Long.class) return long.class;
        if (clazz == Short.class) return short.class;
        if (clazz == Byte.class) return byte.class;
        if (clazz == Void.class) return void.class;
        if (clazz == Character.class) return char.class;
        if (clazz == ArrayList.class) return Collection.class;
        return clazz;
    }

    private static Class<?>[] toParamTypes(Object... params) {
        return Arrays.stream(params)
                .map(obj -> obj != null ? wrapperToPrimitive(obj.getClass()) : null)
                .toArray(Class<?>[]::new);
    }

    @Nullable
    public static Class<?> getClass(@NotNull String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object callConstructorNull(Class<?> clazz, Class<?> paramClass) {
        try {
            Constructor<?> con = clazz.getConstructor(paramClass);
            con.setAccessible(true);
            return con.newInstance(clazz.cast(null));
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object callFirstConstructor(Class<?> clazz, Object... params) {
        try {
            Constructor<?> con = clazz.getConstructors()[0];
            con.setAccessible(true);
            return con.newInstance(params);
        } catch (IllegalAccessException | InstantiationException |
                 InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object callConstructor(Class<?> clazz, Object... params) {
        try {
            Constructor<?> con = clazz.getConstructor(toParamTypes(params));
            con.setAccessible(true);
            return con.newInstance(params);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object callDeclaredConstructor(Class<?> clazz, Object... params) {
        try {
            Constructor<?> con = clazz.getDeclaredConstructor(toParamTypes(params));
            con.setAccessible(true);
            return con.newInstance(params);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object callMethod(Class<?> clazz, String method, Object... params) {
        try {
            Method m = clazz.getMethod(method, toParamTypes(params));
            m.setAccessible(true);
            return m.invoke(null, params);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object callMethod(Object obj, String method, Object... params) {
        try {
            Method m = obj.getClass().getMethod(method, toParamTypes(params));
            m.setAccessible(true);
            return m.invoke(obj, params);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object callDeclaredMethod(Object obj, String method, Object... params) {
        try {
            Method m = obj.getClass().getDeclaredMethod(method, toParamTypes(params));
            m.setAccessible(true);
            return m.invoke(obj, params);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object getField(Object object, String field) {
        try {
            Field f = object.getClass().getField(field);
            f.setAccessible(true);
            return f.get(object);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object getDeclaredField(Class<?> clazz, String field) {
        try {
            Field f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object getDeclaredField(Object object, String field) {
        try {
            Field f = object.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(object);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void setDeclaredField(Object object, String field, Object value) {
        try {
            Field f = object.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(object, value);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get a NMS (net.minecraft.server) class which accepts a package for 1.17 compatibility.
     *
     * @param newPackage the 1.17 package name.
     * @param name       the name of the class.
     * @return the NMS class or null if not found.
     * @since 4.0.0
     */
    @javax.annotation.Nullable
    public static Class<?> getNMSClass(@Nonnull String newPackage, @Nonnull String name) {
        if (supports(17)) name = newPackage + '.' + name;
        return getNMSClass(name);
    }

    /**
     * Get a NMS (net.minecraft.server) class.
     *
     * @param name the name of the class.
     * @return the NMS class or null if not found.
     * @since 1.0.0
     */
    @javax.annotation.Nullable
    public static Class<?> getNMSClass(@Nonnull String name) {
        try {
            return Class.forName(NMS + name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Sends a packet to the player asynchronously if they're online.
     * Packets are thread-safe.
     *
     * @param player  the player to send the packet to.
     * @param packets the packets to send.
     * @return the async thread handling the packet.
     * @see #sendPacketSync(Player, Object...)
     * @since 1.0.0
     */
    @Nonnull
    public static CompletableFuture<Void> sendPacket(@Nonnull Player player, @Nonnull Object... packets) {
        return CompletableFuture.runAsync(() -> sendPacketSync(player, packets))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    /**
     * Sends a packet to the player synchronously if they're online.
     *
     * @param player  the player to send the packet to.
     * @param packets the packets to send.
     * @see #sendPacket(Player, Object...)
     * @since 2.0.0
     */
    public static void sendPacketSync(@Nonnull Player player, @Nonnull Object... packets) {
        try {
            Object handle = GET_HANDLE.invoke(player);
            Object connection = PLAYER_CONNECTION.invoke(handle);

            // Checking if the connection is not null is enough. There is no need to check if the player is online.
            if (connection != null) {
                for (Object packet : packets) SEND_PACKET.invoke(connection, packet);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @javax.annotation.Nullable
    public static Object getHandle(@Nonnull Player player) {
        Objects.requireNonNull(player, "Cannot get handle of null player");
        try {
            return GET_HANDLE.invoke(player);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    @javax.annotation.Nullable
    public static Object getHandle(@Nonnull World world) {
        Objects.requireNonNull(world, "Cannot get handle of null world");
        try {
            return GET_HANDLE_WORLD.invoke(world);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    /**
     * Get a CraftBukkit (org.bukkit.craftbukkit) class.
     *
     * @param name the name of the class to load.
     * @return the CraftBukkit class or null if not found.
     * @since 1.0.0
     */
    @javax.annotation.Nullable
    public static Class<?> getCraftClass(@Nonnull String name) {
        try {
            return Class.forName(CRAFTBUKKIT + name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static final class VersionHandler<T> {
        private int version;
        private T handle;

        private VersionHandler(int version, T handle) {
            if (supports(version)) {
                this.version = version;
                this.handle = handle;
            }
        }

        public VersionHandler<T> v(int version, T handle) {
            if (version == this.version)
                throw new IllegalArgumentException("Cannot have duplicate version handles for version: " + version);
            if (version > this.version && supports(version)) {
                this.version = version;
                this.handle = handle;
            }
            return this;
        }

        public T orElse(T handle) {
            return this.version == 0 ? handle : this.handle;
        }
    }

    private static int toInt(String string, int def) {
        return string.isBlank() ? def : Integer.parseInt(string);
    }
}