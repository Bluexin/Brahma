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

import it.unimi.dsi.fastutil.ints.Int2IntMap
import it.unimi.dsi.fastutil.ints.Int2IntMaps
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.stream.Stream
import kotlin.random.Random

internal class ACLComponentTest {

    @ParameterizedTest
    @MethodSource("map cases")
    fun `serde happy`(m: Int2IntMap) {
        val original = ACLComponent(m)

        val bytes = ByteArrayOutputStream().use {
            DataOutputStream(it).use(original::serializeTo)
            it.flush()
            it.toByteArray()
        }
        assertTrue(original.dirty)
        val read = ByteArrayInputStream(bytes).use {
            DataInputStream(it).use { dis -> ACLComponent().read(dis) }
        }
        m.int2IntEntrySet().forEach { (k, v) ->
            assertEquals(v, read[k])
        }
        assertFalse(read.dirty)
    }

    @Suppress("unused")
    private fun `map cases`(): Stream<Int2IntMap> {
        val rng = Random(Random.nextLong())
        return Stream.of(
            Int2IntMaps.EMPTY_MAP,
            Int2IntOpenHashMap().apply {
                repeat(rng.nextInt(10, 20)) {
                    @Suppress("ReplacePutWithAssignment")
                    put(rng.nextInt(), rng.nextInt())
                }
            },
            Int2IntOpenHashMap().apply {
                repeat(rng.nextInt(100, 200)) {
                    @Suppress("ReplacePutWithAssignment")
                    put(rng.nextInt(), rng.nextInt())
                }
            }
        )
    }
}
