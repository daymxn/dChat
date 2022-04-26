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

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.SQLIntegrityConstraintViolationException

/**
 * Exception type that encompasses all Database specific errors for this project
 *
 * @property errorMessage a message describing the error
 * @property cause an exception that caused the error to occur
 */
sealed class DatabaseError(val errorMessage: String, cause: Throwable) :
  Throwable(errorMessage, cause)

/**
 * Subtype of [DatabaseError] for pre-existing columns on insertion operations.
 *
 * @property cause the [SQLIntegrityConstraintViolationException] that caused the error
 */
data class ColumnAlreadyExists(override val cause: SQLIntegrityConstraintViolationException) :
  DatabaseError("Column already exists", cause)

/**
 * Subtype of [DatabaseError] for any unknown errors
 *
 * @property cause an exception that caused the error to occur
 */
data class UnknownDatabaseError(override val cause: Throwable) :
  DatabaseError("Unknown database error: ${cause.message}", cause)

/**
 * Helper method that converts an [ExposedSQLException] to a [DatabaseError]
 *
 * @param exception the SQL exception to convert
 */
private fun fromSQLError(exception: ExposedSQLException): DatabaseError =
  when (val cause = exception.cause) {
    is SQLIntegrityConstraintViolationException -> ColumnAlreadyExists(cause)
    else -> UnknownDatabaseError(exception)
  }

/**
 * Helper method that converts a [Throwable] to a [DatabaseError]
 *
 * @param throwable the throwable to convert
 */
private fun fromThrowable(throwable: Throwable): DatabaseError =
  when (throwable) {
    is ExposedSQLException -> fromSQLError(throwable)
    else -> UnknownDatabaseError(throwable)
  }

/**
 * Helper method for creating a new [Transaction] that should be caught and converted to
 * [DatabaseError] on failure.
 *
 * @param statement the block to execute within the [Transaction]
 *
 * @return a [Result] that wraps the result of the [Transaction]
 */
suspend fun <T> newQuery(statement: suspend Transaction.() -> T): Result<T> =
  newSuspendedTransaction { kotlin.runCatching { statement() } }.mapError {
    it.printStackTrace()
    fromThrowable(it)
  }
