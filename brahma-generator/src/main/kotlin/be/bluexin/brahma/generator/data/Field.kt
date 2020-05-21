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

package be.bluexin.brahma.generator.data

class Field(
    val name: String,
    val type: String,
    val defaultValue: String? = null,
    val nullable: Boolean = true,
    val transient: Boolean = false
)