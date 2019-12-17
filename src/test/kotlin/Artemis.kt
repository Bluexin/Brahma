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

import be.bluexin.brahma.Noarg
import be.bluexin.brahma.world
import com.artemis.ArchetypeBuilder
import com.artemis.Component
import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.systems.IteratingSystem

// Noarg is needed to tell the kotlin compiler to create an empty constructor automatically
@Noarg
data class Hello(var message: String) : Component()

@Noarg
data class Health(var current: Float, var max: Float) : Component()

// We only want to process entities with the Hello component
@All(Hello::class)
class HelloWorldSystem : IteratingSystem() {
    // Mappers are auto injected
    private lateinit var mHello: ComponentMapper<Hello>

    override fun process(id: Int) = println("Entity $id says `${mHello[id].message}`")
}

@All(Health::class)
class PrintHpSystem : IteratingSystem() {
    private lateinit var mHp: ComponentMapper<Health>

    override fun process(id: Int) = println("Entity $id has ${mHp[id]}")
}

fun main() { // this is ***not*** the pretty way of writing a module !

    // Register any plugins, setup the world.
    val world = world {
        // Add some systems
        with(
            HelloWorldSystem(),
            PrintHpSystem()
        )
    }

    // Create entity. You can do it here or inside systems.
    val entityId = world.create()
    world.edit(entityId).create(Hello::class.java).message = "Hello world!"
    val hp = world.edit(entityId).create(Health::class.java)
    hp.current = 12f
    hp.max = 20f

    // Run the world. HelloWorldSystem should print the hello world message.
    println("1st game loop")
    world.process()

    Thread.sleep(20)

    // Using entity archetypes + mappers (and caching all of those) leads to better performance
    val ourArchetype = ArchetypeBuilder()
        .add(Hello::class.java, Health::class.java)
        .build(world)
    val helloMapper = world.getMapper(Hello::class.java)
    val healthMapper = world.getMapper(Health::class.java)

    val entityId2 = world.create(ourArchetype)
    helloMapper[entityId2].message = "Hello world! 2"
    val hp2 = healthMapper[entityId2]
    hp2.current = 2f
    hp2.max = 10f

    println("2nd game loop")
    world.process()
}
