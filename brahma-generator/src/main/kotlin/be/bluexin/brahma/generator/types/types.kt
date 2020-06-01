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

@file:Suppress("unused")

package be.bluexin.brahma.generator.types

import be.bluexin.brahma.generator.data.Field
import java.util.*

private val typeRegistry = LinkedList<() -> Pair<String, FieldType>>()
val typeMap by lazy { typeRegistry.asSequence().map { it() }.toMap() }

/**
 * Holds information on mapping [Field] data to java code for Brahma SerializedComponents.
 */
abstract class FieldType {
    /**
     * The [Field.type] to resolve
     */
    abstract val key: String

    /**
     * Whether this type supports nullability
     */
    abstract val useNullability: Boolean

    /**
     * Text to use to declare a field of this type
     */
    abstract val typeDef: String

    /**
     * Text to use as default value for fields of this type
     */
    abstract fun default(field: Field): String

    /**
     * Text to use to reset fields of this type
     */
    open fun reset(field: Field): String = "this.${field.name} = ${default(field)}"

    /**
     * Text to use to serialize fields of this type to [java.io.DataOutput]
     */
    abstract fun serialize(field: Field, streamName: String): String

    /**
     * Text to use to deserialize fields of this type to [java.io.DataInput]
     */
    abstract fun deserialize(field: Field, streamName: String): String

    /**
     * Additional imports needed to use this type
     */
    open val imports = arrayOf<String>()

    init {
        typeRegistry += { key to this }
    }
}

object StringType : FieldType() {
    override val key = "string"
    override val useNullability = true
    override val typeDef = "String"

    override fun default(field: Field) = if (field.defaultValue != null) {
        "\"${field.defaultValue}\""
    } else {
        if (field.nullable) "null"
        else error("Non-nullable field ${field.name} needs a defaultValue !")
    }

    override fun serialize(field: Field, streamName: String) =
        "${streamName}.writeUTF(this.${field.name})"

    override fun deserialize(field: Field, streamName: String) =
        "this.${field.name} = ${streamName}.readUTF()"
}
