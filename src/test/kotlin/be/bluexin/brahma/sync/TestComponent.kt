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

import be.bluexin.brahma.Noarg
import be.bluexin.brahma.SerializedComponent
import com.artemis.annotations.PooledWeaver
import java.io.DataInput
import java.io.DataOutput

@PooledWeaver
@Noarg
data class TestComponent(var message: String) : SerializedComponent() {
    override fun serializeTo(outputStream: DataOutput) = outputStream.writeUTF(message)

    override fun deserializeFrom(inputStream: DataInput) {
        message = inputStream.readUTF()
    }

    override fun reset() {
        message = ""
    }
}