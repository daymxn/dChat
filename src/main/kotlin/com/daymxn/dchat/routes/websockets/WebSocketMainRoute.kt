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

package com.daymxn.dchat.routes.websockets

import com.daymxn.dchat.datamodel.User
import com.daymxn.dchat.routes.WebSocketMainRoute
import com.daymxn.dchat.util.WebSocketConnectionManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

fun Route.webSocketMain() {
  webSocket(WebSocketMainRoute) {
    val user = fetchUser(call) ?: return@webSocket

    WebSocketConnectionManager.keepSession(this, user).eventLoop()
  }
}

private suspend fun fetchUser(call: ApplicationCall): User? {
  return call.principal<User>().apply {
    if (this == null) call.respond(HttpStatusCode.Unauthorized) // this shouldn't happen anyways
  }
}
