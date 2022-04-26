/*
 * Copyright 2022 Daymon Littrell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.daymxn.dchat.util

/**
 * Inline helper method that wraps a block in [mapCatching], and handles null values
 *
 * Is highly useful in performing inline validations on retrieved data from the database.
 *
 * @param block the block to wrap around
 *
 * @return a [Result] that wraps the error thrown from the block as a failure, or the result as a
 * success
 */
inline fun <T> Result<T?>.check(block: (value: T?) -> Unit): Result<T> = mapCatching {
  block(it)
  it ?: validationError("Unexpected null value.")
}

/**
 * Inline helper method that wraps a block in an infinite loop, within a [runCatching]
 *
 * Is very rarely used, but provides a more idiomatic interface on an otherwise ugly (but needed)
 * implementation.
 *
 * @param block the block to wrap around
 *
 * @return a [Result] that wraps the error thrown from the block as a failure, or the result as a
 * success
 */
inline fun runCatchingInfiniteLoop(block: () -> Unit): Result<Unit> {
  return runCatching { while (true) block() }
}
