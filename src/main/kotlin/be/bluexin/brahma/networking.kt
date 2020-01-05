/*
 * Copyright (C) 2020 Arnaud 'Bluexin' Solé
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
import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.io.SaveFileFormat
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.readValue
import io.aeron.Aeron
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import mu.KotlinLogging
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

private val WORKER_ID = Random.nextInt() // TODO: this is probably not the right place to set it

@UseExperimental(ObsoleteCoroutinesApi::class)
@Deprecated("Use Artemis")
fun ReceiveChannel<ByteArray>.mapUpdates(): ReceiveChannel<Update<*>> =
    this.mapNotNull { JSON.readValue<Update<*>?>(it) }

@Deprecated("Use Artemis")
fun ReceiveChannel<Update<*>>.mapBytes(): ReceiveChannel<ByteArray> = this.map { JSON.writeValueAsBytes(it) }

@JsonSerialize(using = UpdateSerializer::class)
@JsonDeserialize(using = UpdateDeserializer::class)
@Deprecated("Use Artemis")
data class Update<T : Value>(
    val entityId: Int,
    val component: Component<T>,
    val value: T?,
    val workerId: Int = WORKER_ID
)

@Deprecated("Use Artemis")
class UpdateSerializer : JsonSerializer<Update<*>>() {
    override fun serialize(value: Update<*>, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeNumberField("workerId", value.workerId)
        gen.writeNumberField("entityId", value.entityId)
        gen.writeNumberField("componentId", value.component.id)
        gen.writeObjectField("value", value.value)
        gen.writeEndObject()
    }
}

@Deprecated("Use Artemis")
class UpdateDeserializer : JsonDeserializer<Update<*>?>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Update<*>? {
        val root = p.codec.readTree<JsonNode>(p)
        val workerId = root.get("workerId").asInt()
        if (workerId == WORKER_ID) return null
        @Suppress("UNCHECKED_CAST")
        val component = ComponentStore[root.get("componentId").asInt()] as? Component<Value>
            ?: return null
        val entityId = root.get("entityId").asInt()
        val value = JSON.treeToValue(root.get("value"), component.clazz)
        return Update(entityId, component, value, workerId)
    }
}

@All
class NetworkingSenderSystem : BaseEntitySystem() {
    private val logger = KotlinLogging.logger {}

    private lateinit var writer: SendChannel<ByteArray>
    private lateinit var connection: Job
    private lateinit var aeron: AeronConfig

    private lateinit var mSent: ComponentMapper<SerializedComponent>

    override fun processSystem() {
        val actives = subscription.entities
        if (actives.isEmpty) return

        actives.data.forEach {

        }

//        val ms = System.currentTimeMillis()
        /*actives.data.forEach {
//            val ts = mSent.create(it)
//            ts.timeStamp = ms
        }*/

        val os = ByteArrayOutputStream()
        serializationManager.save(os, SaveFileFormat(actives))
        os.flush()
        writer.offer(os.toByteArray())
    }

    override fun initialize() {
        super.initialize()

        runBlocking { connectAeron() }
    }

    /**
     * Connects the store to the Aeron server.
     */
    @UseExperimental(ExperimentalCoroutinesApi::class)
    suspend fun connectAeron() {
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
            val channel = Channel<ByteArray>(Channel.UNLIMITED)
            writer = channel
            launch(Dispatchers.IO) {
                for (update in aeronConsumer(aeron)) {
                    serializationManager.load(ByteArrayInputStream(update), SaveFileFormat::class.java)
                }
            }
            aeronProducer(aeron, channel)
            Unit
        }
    }
}
