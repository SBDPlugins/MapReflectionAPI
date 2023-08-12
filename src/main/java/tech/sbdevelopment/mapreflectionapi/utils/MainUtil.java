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

public class MainUtil {
    private MainUtil() {
    }

    /**
     * Gets whether this is a headless JDK that doesn't contain the Java AWT library
     *
     * @return True if java.awt is not available
     */
    public static boolean isHeadlessJDK() {
        try {
            Class.forName("java.awt.Color");
            return false;
        } catch (ClassNotFoundException ex) {
            return true;
        }
    }
}
