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
import be.bluexin.brahma.TestComponentsRegistry
import com.artemis.BaseSystem
import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.github.javafaker.Faker
import kotlinx.coroutines.channels.Channel
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

const val ENTITY_MASTER_ID = 42
const val COMPONENT_ID = 8

abstract class ChannelSystemTest {

    @BeforeEach
    fun beforeEach() = componentsRegistry.reset()

    protected val componentsRegistry = TestComponentsRegistry()

    protected fun registerComponent() {
        componentsRegistry[TestComponent::class.java] = COMPONENT_ID
    }

    protected val strings = object : () -> String {
        private val beers = Faker().beer()
        override operator fun invoke() = beers.name()
    }

    protected fun setup(system: (Channel<ByteArray>) -> BaseSystem): Triple<Channel<ByteArray>, EntityMap, World> {
        val channel = Channel<ByteArray>(1)
        val entityMap = EntityMap()
        return Triple(
            channel, entityMap, World(
                WorldConfigurationBuilder()
                    .with(system(channel))
                    .build()
                    .register(entityMap)
                    .register(entityMap.Reverse())
                    .register(ComponentsRegistry::class.java.canonicalName, componentsRegistry)
            )
        )
    }

    /**
     * It is important to handle all serializing manually here, to make sure we respect the internal protocol
     */
    protected fun TestComponent.toBytes(): ByteArray = ByteArrayOutputStream().use { bos ->
        DataOutputStream(bos).use { dos ->
            dos.writeByte(0)
            dos.writeInt(ENTITY_MASTER_ID)
            dos.writeInt(COMPONENT_ID)
            dos.writeUTF(this.message)
        }
        bos.toByteArray()
    }

    /**
     * It is important to handle all serializing manually here, to make sure we respect the internal protocol
     */
    protected fun getDeleteAsBytes(componentId: Int): ByteArray = ByteArrayOutputStream().use { bos ->
        DataOutputStream(bos).use { dos ->
            dos.writeByte(1)
            dos.writeInt(ENTITY_MASTER_ID)
            dos.writeInt(componentId)
        }

        bos.toByteArray()
    }
}