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

package be.bluexin.brahma

import com.artemis.annotations.Wire
import it.unimi.dsi.fastutil.ints.Int2IntFunction
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap

/**
 * Class to [Wire] to get masterId and localId resolution.
 */
@Suppress("MemberVisibilityCanBePrivate")
class EntityMap {

    /**
     * Returns true if this map knows about [masterId], and false otherwise
     */
    operator fun contains(masterId: Int): Boolean = masterToLocal.containsKey(masterId)

    /**
     * Returns true if this map knows about [localId], and false otherwise
     */
    fun containsLocal(localId: Int): Boolean = localToMaster.containsKey(localId)

    /**
     * Get the local ID for [masterId], or [UNKNOWN_ENTITY_ID] if [masterId] is not known
     */
    operator fun get(masterId: Int): Int = masterToLocal[masterId]

    /**
     * Get the master ID for [localId], or [UNKNOWN_ENTITY_ID] if [localId] is not known
     */
    fun getMaster(localId: Int): Int = localToMaster[localId]

    /**
     * Stores the [localId] matching [masterId]
     */
    fun put(masterId: Int, localId: Int) {
        masterToLocal.put(masterId, localId)
        localToMaster.put(localId, masterId)
    }

    /**
     * Removes the [masterId] from this map
     */
    fun removeMaster(masterId: Int) {
        val localId = masterToLocal.remove(masterId)
        if (localId != UNKNOWN_ENTITY_ID) localToMaster.remove(localId)
    }

    /**
     * Removes the [localId] from this map
     */
    fun removeLocal(localId: Int) {
        val masterId = localToMaster.remove(localId)
        if (masterId != UNKNOWN_ENTITY_ID) masterToLocal.remove(masterId)
    }

    /**
     * Operator alias for [put]
     */
    operator fun set(masterId: Int, localId: Int) = this.put(masterId, localId)

    /**
     * Operator alias for [removeMaster]
     */
    operator fun minusAssign(masterId: Int) = this.removeMaster(masterId)

    private val masterToLocal: Int2IntFunction = Int2IntOpenHashMap().apply {
        defaultReturnValue(UNKNOWN_ENTITY_ID)
    }

    private val localToMaster: Int2IntFunction = Int2IntOpenHashMap().apply {
        defaultReturnValue(UNKNOWN_ENTITY_ID)
    }

    /**
     * Wrapper with operators based on local IDs.
     */
    inner class Reverse {

        /**
         * Operator alias for [getMaster]
         */
        operator fun get(localId: Int) = this@EntityMap.getMaster(localId)

        /**
         * Operator alias for [containsLocal]
         */
        operator fun contains(localId: Int) = this@EntityMap.containsLocal(localId)

        /**
         * Operator alias for [removeLocal]
         */
        operator fun minusAssign(localId: Int) = this@EntityMap.removeLocal(localId)
    }

    companion object {
        /**
         * Special value to identify unknown IDs
         */
        const val UNKNOWN_ENTITY_ID = -1
    }
}