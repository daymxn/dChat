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
 * Inline helper method that performs a transform on an exception, wrapping it in a [Result]
 *
 * I'm very surprised this isn't already implemented in the [Result] library, as It's very commonly
 * used- especially in other languages (eg; scala).
 *
 * @param transform a block to call for transforming an exception
 *
 * @return a [Result] that either wraps the error, or returns the result
 */
inline fun <T> Result<T>.mapError(transform: (throwable: Throwable) -> Throwable): Result<T> {
  return when (val exception = this.exceptionOrNull()) {
    null -> this
    else -> transform(exception).asFailure()
  }
}

/** Helper method that wraps a [Throwable] in a failed [Result] */
fun <T> Throwable.asFailure(): Result<T> = Result.failure(this)

/** Helper method that wraps a [Throwable] in a succeed [Result] */
@NotCurrentlyInUse fun <T> T.asSuccess(): Result<T> = Result.success(this)
