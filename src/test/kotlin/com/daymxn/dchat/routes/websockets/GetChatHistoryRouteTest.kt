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

import com.daymxn.dchat.datamodel.Chat
import com.daymxn.dchat.datamodel.Message
import com.daymxn.dchat.routes.GetChatHistoryRequest
import com.daymxn.dchat.routes.GetChatHistoryResponse
import com.daymxn.dchat.service.ChatService
import com.daymxn.dchat.service.MessageService
import com.daymxn.dchat.service.UserService
import com.daymxn.dchat.util.MockDatabaseService
import com.daymxn.dchat.util.WebSocketTestManager
import com.daymxn.dchat.util.asSuccess
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery

class GetChatHistoryRouteTest :
  FreeSpec({
    val testManager = WebSocketTestManager()

    MockDatabaseService().enable()

    suspend fun callHandler(request: GetChatHistoryRequest): GetChatHistoryResponse =
      testManager.invokeHandler(request).shouldBeInstanceOf()

    "request validation" -
      {
        "fails on user not apart of chat" {
          val fakeChat = Chat(1, 1337, 1337, 1)
          coEvery { ChatService.getById(any()) } answers { fakeChat.asSuccess() }

          val response = callHandler(GetChatHistoryRequest(1u, 1u))
          response.error shouldNotBe null
        }
        "fails on internal database error" {
          coEvery { UserService.getByUsernameLike(any()) } throws Exception()

          val response = callHandler(GetChatHistoryRequest(1u, 1u))
          response.error shouldNotBe null
        }
      }

    "works as expected" {
      val fakeMessage = Message(1, 1, 1, 1, "123")
      val fakeChat = Chat(1, 0, 1, 1)
      coEvery { MessageService.getByChatAndAfter(any(), any()) } answers { listOf(fakeMessage) }
      coEvery { ChatService.getById(any()) } answers { fakeChat.asSuccess() }

      val response = callHandler(GetChatHistoryRequest(1u, 1u))
      response.messages?.isEmpty() shouldBe false
    }
  })
