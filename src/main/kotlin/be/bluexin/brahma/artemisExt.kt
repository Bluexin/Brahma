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

import com.artemis.Component
import com.artemis.ComponentMapper
import com.artemis.World
import com.artemis.utils.Bag
import com.artemis.utils.IntBag
import net.mostlyoriginal.api.utils.pooling.ObjectPool
import net.mostlyoriginal.api.utils.pooling.Poolable
import net.mostlyoriginal.api.utils.pooling.Pools

inline fun <reified T : Component> World.getMapper(): ComponentMapper<T> = this.getMapper(T::class.java)

inline fun <reified T : Poolable> getPool(): ObjectPool<T> = Pools.getPool(T::class.java)

inline fun <reified T> bag(capacity: Int): Bag<T> = Bag(T::class.java, capacity)
inline fun <reified T> bag(): Bag<T> = Bag(T::class.java)

operator fun <T> Bag<in T>.plusAssign(e: T) = this.add(e)

operator fun IntBag.plusAssign(e: Int) = this.add(e)