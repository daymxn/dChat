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

import com.daymxn.dchat.routes.WebSocketRequest
import com.daymxn.dchat.routes.WebSocketResponse
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot

internal class WebSocketTestManager {
  val session = mockWebSocketSession()
  val connection = WebSocketConnection(session, GenerateMockObjects.localUser())

  suspend inline fun invokeHandler(request: WebSocketRequest): WebSocketResponse? {
    val response = slot<WebSocketResponse>()
    coEvery { session.sendMessage(capture(response)) } answers {}
    coEvery { session.waitForRequest() } answers { request }

    connection.waitForMessage()

    return if (response.isCaptured) response.captured else null
  }
}

fun mockWebSocketSession(): WebSocketSession = mockk()
