/*
 * This file is part of MapReflectionAPI.
 * Copyright (c) 2023 inventivetalent / SBDevelopment - All Rights Reserved
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

public abstract class MapWrapper extends AbstractMapWrapper {
    protected ArrayImage content;

    public MapWrapper(ArrayImage image) {
        this.content = image;
    }

    public ArrayImage getContent() {
        return content;
    }

    @Override
    public abstract MapController getController();
}