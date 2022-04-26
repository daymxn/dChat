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

import com.daymxn.dchat.datamodel.Activity
import com.daymxn.dchat.datamodel.Chat
import com.daymxn.dchat.datamodel.Message
import com.daymxn.dchat.routes.SendMessageRequest
import com.daymxn.dchat.routes.SendMessageResponse
import com.daymxn.dchat.service.ActivityService
import com.daymxn.dchat.service.ChatService
import com.daymxn.dchat.service.MessageService
import com.daymxn.dchat.util.MockDatabaseService
import com.daymxn.dchat.util.WebSocketTestManager
import com.daymxn.dchat.util.asSuccess
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery

class SendMessageRouteTest :
  FreeSpec({
    val testManager = WebSocketTestManager()

    MockDatabaseService().enable()

    suspend fun callHandler(request: SendMessageRequest): SendMessageResponse =
      testManager.invokeHandler(request).shouldBeInstanceOf()

    coEvery { ActivityService.insert(any()) } answers { firstArg<Activity>().asSuccess() }

    "request validation" -
      {
        "fails on user not apart of chat" {
          val fakeChat = Chat(0, 1337, 1337, 0)
          coEvery { ChatService.getById(any()) } answers { fakeChat.asSuccess() }

          val response = callHandler(SendMessageRequest(0u, "hello world!"))
          response.error shouldNotBe null
        }
        "fails on blank message" {
          val response = callHandler(SendMessageRequest(0u, ""))
          response.error shouldNotBe null
        }
        "fails on internal database error" {
          val fakeChat = Chat(0, 1, 2, 0)
          coEvery { ChatService.getById(any()) } answers { fakeChat.asSuccess() }
          coEvery { MessageService.insert(any()) } answers { null.asSuccess() }

          val response = callHandler(SendMessageRequest(0u, "hello world!"))
          response.error shouldNotBe null
        }
      }

    "works as expected" {
      val fakeChat = Chat(0, 0, 1337, 0)
      coEvery { ChatService.getById(any()) } answers { fakeChat.asSuccess() }
      coEvery { MessageService.insert(any()) } answers { firstArg<Message>().asSuccess() }

      val response = callHandler(SendMessageRequest(0u, "hello world!"))
      response.error shouldBe null
    }
  })
