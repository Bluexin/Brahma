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

import be.bluexin.brahma.getMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ChannelWriteSystemTest : ChannelSystemTest() {

    @Test
    fun `adding a component to an existing entity leads to the component being sent`() {
        this.registerComponent()
        val (channel, entityMap, world) = setup(::ChannelWriteSystem)
        val messageMapper = world.getMapper<TestComponent>()

        val id = world.create()
        entityMap[ENTITY_MASTER_ID] = id
        val component = messageMapper.create(id).apply {
            message = strings()
            dirty = true
        }

        world.process()

        val u = channel.poll()
        assertNotNull(u)
        assertArrayEquals(component.toBytes(), u)
        assertFalse(component.dirty)
    }

    @Test
    fun `updating an existing component leads to the component being sent`() {
        this.registerComponent()
        val (channel, entityMap, world) = setup(::ChannelWriteSystem)
        val messageMapper = world.getMapper<TestComponent>()

        val id = world.create()
        entityMap[ENTITY_MASTER_ID] = id
        messageMapper.create(id).apply {
            message = strings()
            dirty = false // making it look like the component was already present
        }

        world.process()
        @Suppress("ControlFlowWithEmptyBody")
        while (channel.poll() != null);

        val component = messageMapper[id].apply {
            message = strings()
            dirty = true
        }

        world.process()

        val u = channel.poll()
        assertNotNull(u)
        assertArrayEquals(component.toBytes(), u)
        assertFalse(component.dirty)
    }

    @Test
    fun `removing a component from an entity`() {
        fail<Unit>()
    }

    @Test
    fun `deleting an entity`() {
        fail<Unit>()
    }
}