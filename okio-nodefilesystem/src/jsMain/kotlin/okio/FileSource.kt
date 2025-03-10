/*
 * Copyright (C) 2020 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package okio

import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get

internal class FileSource(
  private val fd: Number
) : Source {
  private var position_ = 0L
  private var closed = false

  override fun read(sink: Buffer, byteCount: Long): Long {
    require(byteCount >= 0L) { "byteCount < 0: $byteCount" }
    check(!closed) { "closed" }

    val data = Uint8Array(byteCount.toInt())
    val readByteCount = readSync(
      fd = fd,
      buffer = data,
      length = byteCount,
      offset = 0,
      position = position_.toDouble()
    ).toInt()

    if (readByteCount == 0) return -1L

    position_ += readByteCount

    for (i in 0 until readByteCount) {
      sink.writeByte(data[i].toInt())
    }

    return readByteCount.toLong()
  }

  override fun timeout(): Timeout = Timeout.NONE

  override fun close() {
    if (closed) return
    closed = true
    closeSync(fd)
  }
}
