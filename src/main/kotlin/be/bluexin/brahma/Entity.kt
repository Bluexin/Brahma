/*
 * Copyright (C) 2019 Arnaud 'Bluexin' Solé
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

import it.unimi.dsi.fastutil.ints.Int2ReferenceMap
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap

@Deprecated("Use Artemis")
data class Entity internal constructor(
    val id: Int,
    private val components: Int2ReferenceMap<Value> = Int2ReferenceOpenHashMap()
) {
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Value> get(component: Component<T>): T? = components[component.id] as T?

    operator fun <T : Value> set(component: Component<T>, value: T?) {
        this.load(component, value)
        EntityStore += Update(
            this.id,
            component,
            value
        )
    }

    internal fun <T : Value> load(component: Component<out T>, value: T?) {
        @Suppress("ReplacePutWithAssignment") // would introduce boxing
        if (value != null) components.put(component.id, value)
        else components.remove(component.id)
    }
}
