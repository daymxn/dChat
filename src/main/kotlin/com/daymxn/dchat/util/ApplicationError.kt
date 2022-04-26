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

import com.daymxn.dchat.routes.Response
import com.daymxn.dchat.routes.WebSocketResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.SerializationException
import kotlin.reflect.KClass
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor

/**
 * Exception type that encompasses all Application specific errors for this project
 *
 * @property errorMessage a message describing the error
 */
data class ApplicationError(val errorMessage: String) : Throwable(errorMessage)

/**
 * Helper method to convert a [DatabaseError] to an [ApplicationError]
 *
 * @param errorMessage optional message to override with
 *
 * @return a newly created [ApplicationError]
 */
fun DatabaseError.toApplicationError(errorMessage: String = this.errorMessage): ApplicationError =
  when (this) {
    is ColumnAlreadyExists -> ApplicationError(errorMessage)
    is UnknownDatabaseError -> ApplicationError("Internal database error")
  }

/**
 * Helper method to convert a [Throwable] to an [ApplicationError]
 *
 * @param errorMessage optional message to override with
 *
 * @return a newly created [ApplicationError]
 */
fun Throwable.toApplicationError(
  errorMessage: String = this.message ?: "Internal server error"
): ApplicationError =
  when (this) {
    is DatabaseError -> this.toApplicationError(errorMessage)
    is ApplicationError -> this
    else -> ApplicationError(errorMessage)
  }

/**
 * Inline helper method that wraps a block in a try catch, and responds with a converted error
 *
 * This allows us to have much more readable code, and maintain more DRY principles- at the cost of
 * implementing side effects.
 *
 * @param responseClass a subclass of [Response] to finish calls with on failure
 * @param block the block to wrap around
 */
suspend inline fun <reified T : Response> PipelineContext<*, ApplicationCall>
  .handleApplicationErrors(
  responseClass: KClass<T>,
  block: () -> Unit,
) {
  try {
    block()
  } catch (error: Throwable) {
    this.call.respond(HttpStatusCode.BadRequest, constructResponseObject(responseClass, error))
  }
}

suspend inline fun <reified T : Response> RouteState.handleApplicationErrors(
  responseClass: KClass<T>,
  block: () -> Unit,
) {
  try {
    block()
  } catch (error: Throwable) {
    this.respond(HttpStatusCode.BadRequest, constructResponseObject(responseClass, error))
  }
}

/**
 * Inline helper method that wraps a block in a try catch, and responds with a converted error
 *
 * This allows us to have much more readable code, and maintain more DRY principles- at the cost of
 * implementing side effects.
 *
 * @param responseClass a subclass of [WebSocketResponse] to finish calls with on failure
 * @param block the block to wrap around
 */
suspend inline fun <reified T : WebSocketResponse> WebSocketConnection.handleApplicationError(
  responseClass: KClass<T>,
  block: () -> Unit,
) {
  try {
    block()
  } catch (error: ClosedReceiveChannelException) {
    error.printStackTrace()
    WebSocketConnectionManager.closeSession(this.user.id)
  } catch (error: Throwable) {
    this.session.sendMessage(constructResponseObject(responseClass, error))
  }
}

/**
 * Inline helper method that utilizes reflection to fill out the error property of [Response] based
 * objects.
 *
 * This also applies to [WebSocketResponse] as well, as [WebSocketResponse] is a subclass of
 * [Response].
 *
 * @param responseClass a subclass of [Response] to construct a new object from
 * @param error the error to implement in the response
 */
inline fun <reified T : Response> constructResponseObject(
  responseClass: KClass<T>,
  error: Throwable
): T {
  error.printStackTrace()

  val errorMessage =
    when (error) {
      is ApplicationError -> error.errorMessage
      is SerializationException -> "Invalid argument for request."
      else -> "Unknown error occurred."
    }

  val primaryConstructor = responseClass.primaryConstructor!!
  val param = primaryConstructor.findParameterByName("error")!!

  return responseClass.primaryConstructor?.callBy(mapOf(param to errorMessage))!!
}

/**
 * Helper method that just throws a new [ApplicationError]
 *
 * Allows for more idiomatic code.
 *
 * @param errorMessage the message to apply to the newly created [ApplicationError]
 *
 * @throws ApplicationError the newly created error
 */
fun validationError(errorMessage: String): Nothing = throw ApplicationError(errorMessage)
