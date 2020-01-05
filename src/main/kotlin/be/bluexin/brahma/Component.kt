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

import kotlin.reflect.KProperty

// TODO: centralized way to define component IDs
//  These could be held in a master controller and queried by workers, or whatever

/**
 * Component key structure.
 * @param T generic type for the value assigned to this key, used for typesafe access trough the store
 */
@Deprecated("Use Artemis")
data class Component<T : Value>(
    /**
     * Name for this component.
     * Should be unique for forwards compatibility.
     */
    val name: String,
    /**
     * Unique ID for this component.
     */
    val id: Int,
    /**
     * Class to use for automatic deserialization of this component's value.
     */
    val clazz: Class<T>
) {

    /**
     * Register this component in the [ComponentStore].
     * This is needed only if you need updates for this component.
     */
    fun listen() {
        ComponentStore += this
    }

    companion object {
        /**
         * Shorthand for creating component delegates.
         */
        inline fun <reified T : Value> component(id: Int) =
            ComponentDelegate(id, T::class.java)

        /**
         * Holder for automatic component delegates.
         */
        class ComponentDelegate<T : Value>(private val id: Int, private val clazz: Class<T>) {
            private lateinit var component: Component<T>

            operator fun getValue(receiver: Any, property: KProperty<*>) = component

            operator fun provideDelegate(receiver: Any, property: KProperty<*>) = this.apply {
                component = Component(property.name, this.id, this.clazz)
            }
        }
    }
}
