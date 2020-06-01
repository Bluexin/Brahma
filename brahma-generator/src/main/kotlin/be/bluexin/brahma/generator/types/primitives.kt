/*
 * Copyright (C) 2019-2020 Arnaud 'Bluexin' Solé
 *
 * This file is part of Brahma-generator.
 *
 * Brahma-generator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Brahma-generator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Brahma-generator.  If not, see <https://www.gnu.org/licenses/>.
 */

package be.bluexin.brahma.generator.types

import be.bluexin.brahma.generator.data.Field

object IntType : FieldType() {
    override val key = "int"
    override val useNullability = false
    override val typeDef = "int"

    override fun default(field: Field) =
        if (field.defaultValue != null) field.defaultValue.toString() else "0"

    override fun serialize(field: Field, streamName: String) =
        "${streamName}.writeInt(this.${field.name})"

    override fun deserialize(field: Field, streamName: String) =
        "this.${field.name} = ${streamName}.readInt()"
}

object DoubleType : FieldType() {
    override val key = "double"
    override val useNullability = false
    override val typeDef = "double"

    override fun default(field: Field) =
        if (field.defaultValue != null) field.defaultValue.toString() else "0.0D"

    override fun serialize(field: Field, streamName: String) =
        "${streamName}.writeDouble(this.${field.name})"

    override fun deserialize(field: Field, streamName: String) =
        "this.${field.name} = ${streamName}.readDouble()"
}

object FloatType : FieldType() {
    override val key = "float"
    override val useNullability = false
    override val typeDef = "float"

    override fun default(field: Field) =
        if (field.defaultValue != null) field.defaultValue.toString() else "0.0F"

    override fun serialize(field: Field, streamName: String) =
        "${streamName}.writeFloat(this.${field.name})"

    override fun deserialize(field: Field, streamName: String) =
        "this.${field.name} = ${streamName}.readFloat()"
}

object ByteType : FieldType() {
    override val key = "byte"
    override val useNullability = false
    override val typeDef = "byte"

    override fun default(field: Field) =
        if (field.defaultValue != null) field.defaultValue.toString() else "0"

    override fun serialize(field: Field, streamName: String) =
        "${streamName}.writeByte(this.${field.name})"

    override fun deserialize(field: Field, streamName: String) =
        "this.${field.name} = ${streamName}.readByte()"
}

object BooleanType : FieldType() {
    override val key = "boolean"
    override val useNullability = false
    override val typeDef = "boolean"

    override fun default(field: Field) =
        if (field.defaultValue != null) field.defaultValue.toString() else "false"

    override fun serialize(field: Field, streamName: String) =
        "${streamName}.writeBoolean(this.${field.name})"

    override fun deserialize(field: Field, streamName: String) =
        "this.${field.name} = ${streamName}.readBoolean()"
}

object LongType : FieldType() {
    override val key = "long"
    override val useNullability = false
    override val typeDef = "long"

    override fun default(field: Field) =
        if (field.defaultValue != null) field.defaultValue.toString() else "0"

    override fun serialize(field: Field, streamName: String) =
        "${streamName}.writeLong(this.${field.name})"

    override fun deserialize(field: Field, streamName: String) =
        "this.${field.name} = ${streamName}.readLong()"
}