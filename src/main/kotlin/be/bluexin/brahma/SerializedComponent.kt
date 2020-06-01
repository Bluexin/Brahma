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

import com.artemis.PooledComponent
import com.artemis.annotations.Transient
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

// TODO: delta updates
// TODO: migrate to https://google.github.io/flatbuffers/ ?
@Transient
abstract class SerializedComponent : PooledComponent() {
    fun clean() {
        dirty = false
    }

    fun dirty() {
        dirty = true
    }

    var dirty = true

    @Throws(IOException::class)
    abstract fun serializeTo(outputStream: DataOutput)

    @Throws(IOException::class)
    abstract fun deserializeFrom(inputStream: DataInput)
}

fun <T : SerializedComponent> T.read(buffer: DataInput): T {
    this.deserializeFrom(buffer)
    this.clean()
    return this
}