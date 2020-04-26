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

internal class ChannelReadSystemTest : ChannelSystemTest() {

    @Test
    fun `reading a known component for an unknown Entity leads to an Entity being added to the World and recorded in the Entity ID map`() {
        this.registerComponent()
        val (channel, entityMap, world) = setup(::ChannelReadSystem)
        val messageMapper = world.getMapper<TestComponent>()

        val component = TestComponent(strings())
        channel.offer(component.toBytes())

        world.process()

        assertTrue(ENTITY_MASTER_ID in entityMap)
        assertEquals(component, messageMapper[entityMap[ENTITY_MASTER_ID]])
    }

    @Test
    fun `reading a known component on an existing Entity leads to the component being added to the Entity`() {
        this.registerComponent()
        val (channel, entityMap, world) = setup(::ChannelReadSystem)
        val messageMapper = world.getMapper<TestComponent>()

        val id = world.create()
        entityMap[ENTITY_MASTER_ID] = id
        val component = TestComponent(strings())
        channel.offer(component.toBytes())

        world.process()

        assertEquals(component, messageMapper[id])
    }

    @Test
    fun `reading unknown components leads to no entities being added to the World and nothing to be inserted in the Entity ID map`() {
        val (channel, entityMap, world) = setup(::ChannelReadSystem)
        val messageMapper = world.getMapper<TestComponent>()

        val component = TestComponent(strings())
        channel.offer(component.toBytes())

        world.process()

        assertNull(messageMapper[0])
        assertFalse(ENTITY_MASTER_ID in entityMap)
    }

    @Test
    fun `reading component delete causes it to be deleted from the Entity but without removing the Entity`() {
        registerComponent()
        val (channel, entityMap, world) = setup(::ChannelReadSystem)
        val messageMapper = world.getMapper<TestComponent>()

        val id = world.create()
        messageMapper.create(id).message = strings()
        entityMap[ENTITY_MASTER_ID] = id
        channel.offer(getDeleteAsBytes(COMPONENT_ID))

        world.process()

        assertTrue(world.entityManager.isActive(id))
        assertNull(messageMapper[id])
    }

    @Test
    fun `reading component delete for an unknown entity causes nothing to happen`() {
        val (channel, entityMap, world) = setup(::ChannelReadSystem)

        channel.offer(getDeleteAsBytes(COMPONENT_ID))

        world.process()

        assertFalse(ENTITY_MASTER_ID in entityMap)
    }

    @Test
    fun `reading Entity delete causes it to be deleted from the World and removed from the Entity ID map`() {
        val (channel, entityMap, world) = setup(::ChannelReadSystem)

        val id = world.create()
        entityMap[ENTITY_MASTER_ID] = id
        channel.offer(getDeleteAsBytes(UpdateType.NO_COMPONENT))

        world.process()

        assertFalse(world.entityManager.isActive(id))
        assertFalse(ENTITY_MASTER_ID in entityMap)
    }
}