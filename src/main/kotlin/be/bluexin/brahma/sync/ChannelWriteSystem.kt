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

import be.bluexin.brahma.ComponentsRegistry
import be.bluexin.brahma.EntityMap
import be.bluexin.brahma.SerializedComponent
import com.artemis.ComponentMapper
import com.artemis.World
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

/**
 * System to read updates from the [World] and send them to the given [outbound] [SendChannel].
 */
@All
class ChannelWriteSystem(
    private val outbound: SendChannel<ByteArray>
) : IteratingSystem() {
    @Wire
    private lateinit var entityMap: EntityMap.Reverse

    @Wire
    private lateinit var componentsRegistry: ComponentsRegistry

    private lateinit var mappers: Sequence<ComponentMapper<out SerializedComponent>>

    override fun initialize() {
        mappers = componentsRegistry.components.map { world.getMapper(it) }.asSequence()
    }

    @UseExperimental(ExperimentalCoroutinesApi::class)
    override fun checkProcessing() = !outbound.isClosedForSend

    // TODO: track & send component/entity deletes

    @UseExperimental(ExperimentalCoroutinesApi::class)
    override fun process(entityId: Int) {
        check(!outbound.isClosedForSend) { "Inbound channel was closed (${outbound.close()})" }
        mappers.map { it[entityId] }.filter(SerializedComponent::dirty).onEach(SerializedComponent::clean)
            .map { it.asBytes(entityId) }.forEach { outbound.offer(it) }
    }

    private fun SerializedComponent.asBytes(entityId: Int) = ByteArrayOutputStream().use { bos ->
        DataOutputStream(bos).use { dos ->
            UpdateType.COMPONENT.serialize(dos)
            dos.writeInt(entityMap[entityId])
            dos.writeInt(componentsRegistry[this::class.java])
            this.serializeTo(dos)
        }
        bos.toByteArray()
    }
}