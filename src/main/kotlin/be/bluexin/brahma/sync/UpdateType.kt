/*
 * Copyright (C) 2019-2020 Arnaud 'Bluexin' Solé
 *
 * This file is part of Brahma.
 *
 * Brahma is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Brahma is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Brahma.  If not, see <https://www.gnu.org/licenses/>.
 */

package be.bluexin.brahma.sync

import java.io.DataInput
import java.io.DataOutput

/**
 * Update type for our internal protocol
 */
enum class UpdateType {
    /**
     * Denotes a component update
     */
    COMPONENT,

    /**
     * Denotes an entity deletion
     */
    DELETE;

    /**
     * Serialize this update type
     *
     * @param to the target
     */
    fun serialize(to: DataOutput) {
        to.writeByte(this.ordinal)
    }

    companion object {
        /**
         * Deserialize an update type
         *
         * @param from the input to read from
         */
        fun read(from: DataInput): UpdateType = values()[from.readByte().toInt()]

        /**
         * Flag to mark Entity Deletions (as opposed to Component Deletions)
         */
        const val NO_COMPONENT = -1
    }
}