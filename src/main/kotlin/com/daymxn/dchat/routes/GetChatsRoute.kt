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

fun Route.getChatsForUser() {
  get(GetChatsRoute) { getChatsRouteHandler(routeState()) }
}

val getChatsRouteHandler: RouteHandler = {
  handleApplicationErrors(GetChatsResponse::class) {
    validateRequest(getRequest(), getUser()).also {
      DatabaseManager.getChatsForUserSince(it.user, it.since).apply {
        respond(HttpStatusCode.Accepted, GetChatsResponse(this))
      }
    }
  }
}

private fun validateRequest(request: GetChatsRequest, user: User?): ValidatedGetChatsRequest =
  ValidatedGetChatsRequest(validateUser(user), validateSince(request.since))

private fun validateUser(user: User?) =
  user ?: validationError("You must be logged in to access this.")

private fun validateSince(since: ULong) = since.toLong()

private data class ValidatedGetChatsRequest(val user: User, val since: Long)
