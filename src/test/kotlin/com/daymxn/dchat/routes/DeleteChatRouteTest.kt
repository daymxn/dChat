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

import com.daymxn.dchat.service.ChatService
import com.daymxn.dchat.util.ApplicationTestManager
import com.daymxn.dchat.util.MockDatabaseService
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery

class DeleteChatRouteTest :
  FreeSpec({
    val testManager = ApplicationTestManager(deleteChatRouteHandler)
    MockDatabaseService().enable()

    suspend fun callHandler(request: DeleteChatRequest): DeleteChatResponse =
      testManager.invokeHandler(request).shouldBeInstanceOf()

    "request validation" -
      {
        "fails on user not logged in" {
          testManager.setUserIsLoggedIn(false)

          val response = callHandler(DeleteChatRequest(1u))
          response.error shouldNotBe null
        }
        "fails on user not apart of chat" {
          testManager.setUserIsLoggedIn()

          coEvery { ChatService.deleteIfUserApartOf(any(), any()) } returns false

          val response = callHandler(DeleteChatRequest(1u))
          response.error shouldNotBe null
        }
        "fails on internal database error" {
          testManager.setUserIsLoggedIn()

          coEvery { ChatService.deleteIfUserApartOf(any(), any()) } throws Exception()

          val response = callHandler(DeleteChatRequest(1u))
          response.error shouldNotBe null
        }
      }

    "works as expected" {
      testManager.setUserIsLoggedIn()

      coEvery { ChatService.deleteIfUserApartOf(any(), any()) } returns true

      val response = callHandler(DeleteChatRequest(1u))
      response.error shouldBe null
    }
  })
