/*
 * UnsignedInt.kt
 *
 * Copyright (C) 2017 Odyssey Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.funwithnativecode

import com.sun.jna.IntegerType

/**
 * Unsigned Int.
 */
class UInt(value: Long = 0) : IntegerType(SIZE, value, true) {

    companion object {
        private const val SIZE = 4
    }

    operator fun times(other: Int): Int {
        return toInt() * other
    }

    operator fun times(other: UInt): Int {
        return toInt() * other.toInt()
    }

    override fun toByte(): Byte = toInt().toByte()

    override fun toChar(): Char = toInt().toChar()

    override fun toShort(): Short = toInt().toShort()
}
