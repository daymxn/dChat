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

package com.daymxn.dchat.routes

import com.daymxn.dchat.DatabaseManager
import com.daymxn.dchat.datamodel.User
import com.daymxn.dchat.util.RouteHandler
import com.daymxn.dchat.util.handleApplicationErrors
import com.daymxn.dchat.util.routeState
import com.daymxn.dchat.util.validationError
import io.ktor.http.*
import io.ktor.server.routing.*

fun Route.startChat() {
  post(StartChatRoute) { startChatRouteHandler(routeState()) }
}

val startChatRouteHandler: RouteHandler = {
  handleApplicationErrors(StartChatResponse::class) {
    validateRequest(getRequest(), getUser()).also {
      DatabaseManager.startChatForUserWith(it.user, it.receipt).apply {
        respond(HttpStatusCode.Accepted, StartChatResponse(this))
      }
    }
  }
}

private fun validateRequest(request: StartChatRequest, user: User?): ValidatedStartChatRequest =
  ValidatedStartChatRequest(validateUser(user), validateReceipt(request.receipt)).also {
    validateNotSelf(it.user, it.receipt)
  }

private fun validateNotSelf(user: User, receiver: Long) =
  user.takeIf { it.id != receiver } ?: validationError("You can not start a chat with yourself.")

private fun validateUser(user: User?) =
  user ?: validationError("You must be logged in to access this.")

private fun validateReceipt(id: ULong) = id.toLong()

private data class ValidatedStartChatRequest(val user: User, val receipt: Long)
