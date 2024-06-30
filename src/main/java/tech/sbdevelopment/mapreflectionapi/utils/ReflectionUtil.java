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

package tech.sbdevelopment.mapreflectionapi.utils;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.cryptomorin.xseries.reflection.XReflection.getCraftClass;
import static com.cryptomorin.xseries.reflection.XReflection.getNMSClass;

public class ReflectionUtil {
    private static final Map<String, Constructor<?>> constructorCache = new HashMap<>();
    private static final Map<String, Method> methodCache = new HashMap<>();
    private static final Map<String, Field> fieldCache = new HashMap<>();
    private static final Class<?> craftWorld = getCraftClass("CraftWorld");

    /**
     * Helper class converted to {@link List}
     *
     * @param <E> The storage type
     */
    public static class ListParam<E> extends ArrayList<E> {
    }

    /**
     * Helper class converted to {@link Collection}
     *
     * @param <E> The storage type
     */
    public static class CollectionParam<E> extends ArrayList<E> {
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
        if (clazz == CollectionParam.class) return Collection.class;
        if (clazz == ListParam.class) return List.class;
        if (clazz == ArrayList.class) return Collection.class; //LEGACY!
        if (clazz == HashMap.class) return Map.class;
        return clazz;
    }

    private static Class<?>[] toParamTypes(Object... params) {
        return Arrays.stream(params)
                .map(obj -> obj != null ? wrapperToPrimitive(obj.getClass()) : null)
                .toArray(Class<?>[]::new);
    }

    @Nullable
    public static Object getHandle(@NotNull World world) {;
        return callDeclaredMethod(craftWorld, world, "getHandle");
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
            String cacheKey = "ConstructorNull:" + clazz.getName() + ":" + paramClass.getName();

            if (constructorCache.containsKey(cacheKey)) {
                Constructor<?> cachedConstructor = constructorCache.get(cacheKey);
                return cachedConstructor.newInstance(clazz.cast(null));
            } else {
                Constructor<?> con = clazz.getConstructor(paramClass);
                con.setAccessible(true);
                constructorCache.put(cacheKey, con);
                return con.newInstance(clazz.cast(null));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object callFirstConstructor(Class<?> clazz, Object... params) {
        try {
            String cacheKey = "FirstConstructor:" + clazz.getName();

            if (constructorCache.containsKey(cacheKey)) {
                Constructor<?> cachedConstructor = constructorCache.get(cacheKey);
                return cachedConstructor.newInstance(params);
            } else {
                Constructor<?> con = clazz.getConstructors()[0];
                con.setAccessible(true);
                constructorCache.put(cacheKey, con);
                return con.newInstance(params);
            }
        } catch (IllegalAccessException | InstantiationException |
                 InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object callConstructor(Class<?> clazz, Object... params) {
        try {
            String cacheKey = "Constructor:" + clazz.getName() + ":" + Arrays.hashCode(params);

            if (constructorCache.containsKey(cacheKey)) {
                Constructor<?> cachedConstructor = constructorCache.get(cacheKey);
                return cachedConstructor.newInstance(params);
            } else {
                Constructor<?> con = clazz.getConstructor(toParamTypes(params));
                con.setAccessible(true);
                constructorCache.put(cacheKey, con);
                return con.newInstance(params);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object callDeclaredConstructor(Class<?> clazz, Object... params) {
        try {
            String cacheKey = "DeclaredConstructor:" + clazz.getName() + ":" + Arrays.hashCode(params);

            if (constructorCache.containsKey(cacheKey)) {
                Constructor<?> cachedConstructor = constructorCache.get(cacheKey);
                return cachedConstructor.newInstance(params);
            } else {
                Constructor<?> con = clazz.getDeclaredConstructor(toParamTypes(params));
                con.setAccessible(true);
                constructorCache.put(cacheKey, con);
                return con.newInstance(params);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object callMethod(Class<?> clazz, String method, Object... params) {
        try {
            String cacheKey = "Method:" + clazz.getName() + ":" + method + ":" + Arrays.hashCode(params);

            if (methodCache.containsKey(cacheKey)) {
                Method cachedMethod = methodCache.get(cacheKey);
                return cachedMethod.invoke(null, params);
            } else {
                Method m = clazz.getMethod(method, toParamTypes(params));
                m.setAccessible(true);
                methodCache.put(cacheKey, m);
                return m.invoke(null, params);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object callMethod(Object obj, String method, Object... params) {
        try {
            String cacheKey = "Method:" + obj.getClass().getName() + ":" + method + ":" + Arrays.hashCode(params);

            if (methodCache.containsKey(cacheKey)) {
                Method cachedMethod = methodCache.get(cacheKey);
                return cachedMethod.invoke(obj, params);
            } else {
                Method m = obj.getClass().getMethod(method, toParamTypes(params));
                m.setAccessible(true);
                methodCache.put(cacheKey, m);
                return m.invoke(obj, params);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object callDeclaredMethod(Object obj, String method, Object... params) {
        try {
            String cacheKey = "DeclaredMethod:" + obj.getClass().getName() + ":" + method + ":" + Arrays.hashCode(params);

            if (methodCache.containsKey(cacheKey)) {
                Method cachedMethod = methodCache.get(cacheKey);
                return cachedMethod.invoke(obj, params);
            } else {
                Method m = obj.getClass().getDeclaredMethod(method, toParamTypes(params));
                m.setAccessible(true);
                methodCache.put(cacheKey, m);
                return m.invoke(obj, params);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object callDeclaredMethod(Class<?> clazz, Object obj, String method, Object... params) {
        try {
            String cacheKey = "DeclaredMethod:" + clazz.getName() + ":" + method + ":" + Arrays.hashCode(params);

            if (methodCache.containsKey(cacheKey)) {
                Method cachedMethod = methodCache.get(cacheKey);
                return cachedMethod.invoke(obj, params);
            } else {
                Method m = clazz.getDeclaredMethod(method, toParamTypes(params));
                m.setAccessible(true);
                methodCache.put(cacheKey, m);
                return m.invoke(obj, params);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean hasField(Object packet, String field) {
        try {
            String cacheKey = "HasField:" + packet.getClass().getName() + ":" + field;

            if (fieldCache.containsKey(cacheKey)) {
                return true;
            } else {
                packet.getClass().getDeclaredField(field);
                fieldCache.put(cacheKey, null);
                return true;
            }
        } catch (NoSuchFieldException ex) {
            return false;
        }
    }

    @Nullable
    public static Object getField(Object object, String field) {
        try {
            String cacheKey = "Field:" + object.getClass().getName() + ":" + field;

            if (fieldCache.containsKey(cacheKey)) {
                Field cachedField = fieldCache.get(cacheKey);
                return cachedField.get(object);
            } else {
                Field f = object.getClass().getField(field);
                f.setAccessible(true);
                fieldCache.put(cacheKey, f);
                return f.get(object);
            }
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object getDeclaredField(Class<?> clazz, String field) {
        try {
            String cacheKey = "DeclaredField:" + clazz.getName() + ":" + field;

            if (fieldCache.containsKey(cacheKey)) {
                Field cachedField = fieldCache.get(cacheKey);
                return cachedField.get(null);
            } else {
                Field f = clazz.getDeclaredField(field);
                f.setAccessible(true);
                fieldCache.put(cacheKey, f);
                return f.get(null);
            }
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object getDeclaredField(Object object, String field) {
        try {
            String cacheKey = "DeclaredField:" + object.getClass().getName() + ":" + field;

            if (fieldCache.containsKey(cacheKey)) {
                Field cachedField = fieldCache.get(cacheKey);
                return cachedField.get(object);
            } else {
                Field f = object.getClass().getDeclaredField(field);
                f.setAccessible(true);
                fieldCache.put(cacheKey, f);
                return f.get(object);
            }
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object getDeclaredField(Class<?> clazz, Object object, String field) {
        try {
            String cacheKey = "DeclaredField:" + clazz.getName() + ":" + field;

            if (fieldCache.containsKey(cacheKey)) {
                Field cachedField = fieldCache.get(cacheKey);
                return cachedField.get(object);
            } else {
                Field f = clazz.getDeclaredField(field);
                f.setAccessible(true);
                fieldCache.put(cacheKey, f);
                return f.get(object);
            }
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void setDeclaredField(Object object, String field, Object value) {
        try {
            String cacheKey = "DeclaredField:" + object.getClass().getName() + ":" + field;

            if (fieldCache.containsKey(cacheKey)) {
                Field cachedField = fieldCache.get(cacheKey);
                cachedField.set(object, value);
            } else {
                Field f = object.getClass().getDeclaredField(field);
                f.setAccessible(true);
                fieldCache.put(cacheKey, f);
                f.set(object, value);
            }
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
}