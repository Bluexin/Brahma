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

package be.bluexin.brahma

import io.aeron.Aeron
import io.aeron.FragmentAssembler
import io.aeron.Publication
import io.aeron.logbuffer.FragmentHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import mu.KotlinLogging.logger
import org.agrona.concurrent.BackoffIdleStrategy
import org.agrona.concurrent.IdleStrategy
import org.agrona.concurrent.UnsafeBuffer
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

// TODO: investigate the use of Exclusive* stuff (https://github.com/real-logic/aeron/wiki/Client-Concurrency-Model)

data class AeronConfig(
    val client: Aeron,
    val url: String,
    val stream: Int,
    val bufferSize: Int = 1024,
    val idleStrategy: IdleStrategy = BackoffIdleStrategy(
        100,
        10,
        TimeUnit.MICROSECONDS.toNanos(1),
        TimeUnit.MICROSECONDS.toNanos(100)
    )
)

/**
 * Creates an Aeron producer, relaying everything from [input] to the Aeron bus.
 */
@ExperimentalCoroutinesApi
fun CoroutineScope.aeronProducer(config: AeronConfig, input: ReceiveChannel<ByteArray>): Job = launch(Dispatchers.IO) {
    val logger = logger("Aeron producer")
    logger.info { "Booting up the Aeron producer" }

    val pub = config.client.addPublication(config.url, config.stream)
    val buff = UnsafeBuffer(ByteBuffer.allocateDirect(config.bufferSize))
    val idleStrategy = config.idleStrategy

    logger.info { "Starting to send to Aeron" }

    for (i in input) {
        buff.putBytes(0, i)
        var res = pub.offer(buff, 0, i.size)
        while (isActive && res <= 0) {
            when (res) {
                Publication.CLOSED -> {
                    input.cancel()
                    return@launch
                }
                else -> {
                    idleStrategy.idle()
                    res = pub.offer(buff, 0, i.size)
                }
            }
        }
        if (!isActive) break
        idleStrategy.reset()
    }

    pub.close()

    logger.info { "Stopping to send to Aeron (canceled: ${!isActive}, input closed: ${input.isClosedForReceive})" }
}

/**
 * Creates an Aeron consumer, relaying everything from the Aeron bus to the returned Channel.
 */
@ExperimentalCoroutinesApi
fun CoroutineScope.aeronConsumer(config: AeronConfig): ReceiveChannel<ByteArray> = produce(Dispatchers.IO, capacity = Channel.UNLIMITED) {
    val logger = logger("Aeron consumer")
    logger.info { "Booting up the Aeron consumer" }

    val idleStrategy = config.idleStrategy
    val sub = config.client.addSubscription(config.url, config.stream)
    val fragmentHandler = FragmentAssembler(FragmentHandler { buffer, offset, length, _ ->
        val data = ByteArray(length)
        buffer.getBytes(offset, data)
        offer(data)
    })

    logger.info { "Starting to consume from Aeron" }

    while (isActive) {
        val fragmentsRead = sub.poll(fragmentHandler, 10)
        if (isActive) idleStrategy.idle(fragmentsRead)
    }

    sub.close()

    logger.info { "Stopping to consume from Aeron (canceled: ${!isActive}, output closed: ${channel.isClosedForSend})" }
}
