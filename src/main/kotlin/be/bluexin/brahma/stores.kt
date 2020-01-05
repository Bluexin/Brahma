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

import be.bluexin.kaeron.AeronConfig
import be.bluexin.kaeron.aeronConsumer
import be.bluexin.kaeron.aeronProducer
import io.aeron.Aeron
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import mu.KotlinLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.coroutines.coroutineContext

@Deprecated("Use Artemis")
object ComponentStore {
    private val logger = KotlinLogging.logger {}

    private val components: Int2ReferenceMap<Component<*>> = Int2ReferenceOpenHashMap()

    /**
     * Get a [Component] from the store by ID, or null if no Component with given ID is found.
     */
    operator fun get(componentId: Int): Component<*>? = components[componentId]

    internal operator fun plusAssign(component: Component<*>) {
        @Suppress("ReplacePutWithAssignment") // would introduce boxing
        val old = components.put(component.id, component)
        if (old != components.defaultReturnValue()) logger.warn { "Component $component replaced $old in the store !" }
    }
}

@Deprecated("Use Artemis")
object EntityStore {
    private val logger = KotlinLogging.logger {}
    private lateinit var writer: SendChannel<Update<*>>
    private lateinit var connection: Job
    private lateinit var aeron: AeronConfig

    private val entities: Int2ReferenceMap<Entity> = Int2ReferenceOpenHashMap()

    /**
     * Get an [Entity] from the store by ID.
     * If it doesn't exist, it will automatically be created.
     */
    operator fun get(entityId: Int): Entity = entities.computeIfAbsentPartial(entityId) {
        Entity(
            it
        )
    }

    internal operator fun plusAssign(update: Update<*>) {
        writer.offer(update)
    }

    /**
     * Connects the store to the Aeron server.
     * This is a non-blocking async call, meant for usage from Java.
     * The returned Future will be completed after the store is ready to start sending updates.
     */
    fun connectAeronFuture(): Future<Unit> = CompletableFuture<Unit>().also {
        GlobalScope.launch { connectAeron(it) }
    }

    /**
     * Connects the store to the Aeron server.
     */
    @UseExperimental(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
    suspend fun connectAeron(future: CompletableFuture<Unit>? = null) {
        aeron = AeronConfig(
            client = Aeron.connect(
                Aeron.Context()
                    .errorHandler { logger.warn(it) { "Aeron error" } }
                    .availableImageHandler { logger.info { "Aeron is available" } }
                    .unavailableImageHandler { logger.info { "Aeron went down" } }
            ),
            url = "aeron:udp?endpoint=localhost:40123", // TODO: config
            stream = 10
        )
        connection = Job()
        with(CoroutineScope(coroutineContext + connection)) {
            val channel = Channel<Update<*>>(Channel.UNLIMITED)
            writer = channel
            launch(Dispatchers.IO) {
                for (update in aeronConsumer(aeron).mapUpdates()) {
                    this@EntityStore[update.entityId].load(
                        update.component,
                        update.value
                    )
                }
            }
            aeronProducer(aeron, channel.mapBytes())
            Unit
        }
        future?.complete(Unit)
    }


    /**
     * Disconnects the store from the Aeron server.
     * This is a blocking call, meant for usage from Java.
     */
    fun disconnectAeronBlocking() = runBlocking { disconnectAeron() }

    /**
     * Disconnects the store from the Aeron server.
     */
    suspend fun disconnectAeron() {
        logger.info { "Disconnecting..." }
        connection.cancelAndJoin()
        logger.info { "Closed connection" }
        aeron.client.close()
    }
}
