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

import com.daymxn.dchat.datamodel.User
import com.daymxn.dchat.routes.*
import com.daymxn.dchat.routes.websockets.getChatHistoryRoute
import com.daymxn.dchat.routes.websockets.getUsersForSubstringRoute
import com.daymxn.dchat.routes.websockets.sendMessageRoute
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.*

/**
 * Static object that maintains a Synchronized Map of all active [WebSocketConnection]
 *
 * Is essentially just a *cleaner* wrapper around a single instance of [Collections.synchronizedMap]
 */
object WebSocketConnectionManager {
  private val connections: MutableMap<Long, WebSocketConnection> =
    Collections.synchronizedMap(LinkedHashMap())

  /**
   * Creates a new [WebSocketConnection] to keep track of, limited to one per [User]
   *
   * Will close out any previously open [WebSocketConnection] for the specified [User] (if any at
   * all).
   *
   * @param session the [DefaultWebSocketServerSession] to wrap around
   * @param user the user to create a connection for
   *
   * @return the newly created [WebSocketConnection]
   */
  suspend fun keepSession(session: DefaultWebSocketServerSession, user: User): WebSocketConnection =
    with(WebSocketConnection(WebSocketSession(session), user)) {
      closeSession(user.id)
      connections[user.id] = this
      this
    }

  /**
   * Closes out a [WebSocketConnection] by a [User] ID
   *
   * Will also send a [CloseReason] frame to the websocket, if it is still open.
   *
   * @param id the ID of the [User] to close
   */
  suspend fun closeSession(id: Long) {
    connections.remove(id)?.session?.close()
  }

  /**
   * Sends a message to a specified user, via a previously established [WebSocketConnection]
   *
   * Does nothing in the case that the [User] does not currently have a [WebSocketConnection] open.
   *
   * @param client the ID of the [User] to send the message to
   * @param message the [WebSocketRequest] to send to the user
   */
  suspend fun notifyClient(client: Long, message: WebSocketRequest) {
    connections[client]?.session?.sendMessage(message)
  }
}

/**
 * Wrapper around a [DefaultWebSocketServerSession] for a specified [User]
 *
 * Also exposes some lifecycle methods for the web socket.
 *
 * @property session the connected socket session
 * @property user the user to whom this connection belongs
 */
class WebSocketConnection(val session: WebSocketSession, val user: User) {

  /**
   * Waits in an infinite loop for a [WebSocketRequest], and responds accordingly
   *
   * This is the main lifecycle of a [WebSocketConnection], and should live in a unique coroutine
   * per connection. The loop will also clean up after itself by catching exceptions as needed, and
   * closing out the session from [WebSocketConnectionManager].
   */
  suspend fun eventLoop() {
    runCatchingInfiniteLoop { waitForMessage() }.onFailure {
      WebSocketConnectionManager.closeSession(user.id)
    }
  }

  /**
   * Waits for a [WebSocketRequest], and processes it when received
   *
   * Will deserialize all incoming messages, and facilitate requests to their unique methods for
   * handling the request.
   */
  internal suspend fun waitForMessage() {
    when (val request = session.waitForRequest()) {
      is GetUsersForSubstringRequest ->
        handleApplicationError(GetUsersForSubstringResponse::class) {
          getUsersForSubstringRoute(request)
        }
      is SendMessageRequest ->
        handleApplicationError(SendMessageResponse::class) { sendMessageRoute(request) }
      is GetChatHistoryRequest ->
        handleApplicationError(GetChatHistoryResponse::class) { getChatHistoryRoute(request) }
    }
  }
}

class WebSocketSession(val session: DefaultWebSocketServerSession) {
  suspend fun waitForRequest(): WebSocketRequest = session.receiveDeserialized()

  suspend fun sendMessage(message: WebSocketHeader) = session.sendSerialized(message)

  suspend fun close() =
    session.close(CloseReason(CloseReason.Codes.NORMAL, "Server requested to close this socket."))
}
