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

import com.daymxn.dchat.datamodel.Chat
import com.daymxn.dchat.service.ChatService
import com.daymxn.dchat.util.*
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk

class StartChatRouteTest :
  FreeSpec({
    val testManager = ApplicationTestManager(startChatRouteHandler)
    MockDatabaseService().enable()

    suspend fun callHandler(request: StartChatRequest): StartChatResponse =
      testManager.invokeHandler(request).shouldBeInstanceOf()

    "request validation" -
      {
        "fails on user not logged in" {
          testManager.setUserIsLoggedIn(false)

          val response = callHandler(StartChatRequest(2u))
          response.error shouldNotBe null
        }
        "fails on starting chat with self" {
          testManager.setUserIsLoggedIn()

          val response = callHandler(StartChatRequest(0u))
          response.error shouldNotBe null
        }
        "fails on internal database error" {
          testManager.setUserIsLoggedIn()
          testManager.saveUser(GenerateMockObjects.user(2))

          coEvery { ChatService.insert(any()) } answers { null.asSuccess() }

          val response = callHandler(StartChatRequest(2u))
          response.error shouldNotBe null
        }
        "fails when a chat already exists" {
          testManager.setUserIsLoggedIn()
          testManager.saveUser(GenerateMockObjects.user(2))

          coEvery { ChatService.insert(any()) } answers { ColumnAlreadyExists(mockk()).asFailure() }

          val response = callHandler(StartChatRequest(2u))
          response.error shouldNotBe null
        }
      }

    "works as expected" {
      testManager.setUserIsLoggedIn()
      val testUser = GenerateMockObjects.user(2)
      testManager.saveUser(testUser)

      coEvery { ChatService.insert(any()) } answers { firstArg<Chat>().asSuccess() }

      val response = callHandler(StartChatRequest(2u))
      response.chat shouldNotBe null
    }
  })
