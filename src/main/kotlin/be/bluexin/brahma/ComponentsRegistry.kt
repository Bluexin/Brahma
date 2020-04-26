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
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntFunction
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

/**
 * Class to [Wire] to get [SerializedComponent] mapping to their numerical ID for (de)serialization purposes.
 */
open class ComponentsRegistry { // TODO: get registry from master node
    private var currentId = 0
    protected val registry: Object2IntFunction<Class<out SerializedComponent>> = Object2IntOpenHashMap()
    protected val reverseRegistry: Int2ObjectFunction<Class<out SerializedComponent>> = Int2ObjectOpenHashMap()

    /**
     * Register [component], automatically assigning an ID
     */
    fun register(component: Class<out SerializedComponent>) {
        this[component]
    }

    /**
     * Generic alias for [register]
     */
    inline fun <reified T : SerializedComponent> register() = this.register(T::class.java)

    /**
     * Operator alias for [register]
     */
    operator fun plusAssign(component: Class<out SerializedComponent>) = this.register(component)

    /**
     * Get the stored ID for the given [component], first registering it if missing
     */
    operator fun get(component: Class<out SerializedComponent>): Int =
        if (component in this) this.registry.getInt(component) else {
            this.registry.put(component, currentId)
            this.reverseRegistry.put(currentId, component)
            currentId++
        }

    /**
     * Get the component registered with [id], or null if it is unknown
     */
    operator fun get(id: Int): Class<out SerializedComponent>? = this.reverseRegistry.get(id)

    /**
     * Returns true if a component is registered with [id]
     */
    operator fun contains(id: Int): Boolean = reverseRegistry.containsKey(id)

    /**
     * Returns true if the [component] is registered
     */
    operator fun contains(component: Class<out SerializedComponent>): Boolean = registry.containsKey(component)

    /**
     * Returns a [Set] containing all known [SerializedComponent]s
     */
    val components: Set<Class<out SerializedComponent>> get() = (registry as Object2IntMap<Class<out SerializedComponent>>).keys
}

/**
 * Class to instantiate to get a test-oriented [ComponentsRegistry].
 * Register it with `ComponentsRegistry::class.java.canonicalName` as name so it gets [Wire]d in place of the original.
 */
class TestComponentsRegistry : ComponentsRegistry() {
    /**
     * Force the [component] to be registered as [id] (as opposed to assigning an automatic ID)
     */
    operator fun set(component: Class<out SerializedComponent>, id: Int) {
        this.registry.put(component, id)
        this.reverseRegistry.put(id, component)
    }

    /**
     * Clear the registry
     */
    fun reset() {
        registry.clear()
        reverseRegistry.clear()
    }
}