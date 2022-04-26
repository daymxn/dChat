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
import io.mockk.mockk

class GetChatsRouteTest :
  FreeSpec({
    val testManager = ApplicationTestManager(getChatsRouteHandler)
    MockDatabaseService().enable()

    suspend fun callHandler(request: GetChatsRequest): GetChatsResponse =
      testManager.invokeHandler(request).shouldBeInstanceOf()

    "request validation" -
      {
        "fails on user not logged in" {
          testManager.setUserIsLoggedIn(false)

          val response = callHandler(GetChatsRequest())
          response.error shouldNotBe null
        }
        "fails on internal database error" {
          testManager.setUserIsLoggedIn()
          coEvery { ChatService.getByUserAndAfter(any(), any()) } throws Exception()

          val response = callHandler(GetChatsRequest())
          response.error shouldNotBe null
        }
      }

    "works as expected" -
      {
        "when there are no chats" {
          testManager.setUserIsLoggedIn()
          coEvery { ChatService.getByUserAndAfter(any(), any()) } answers { emptyList() }

          val response = callHandler(GetChatsRequest())
          response.chats shouldNotBe null
        }
        "when there are chats" {
          testManager.setUserIsLoggedIn()
          coEvery { ChatService.getByUserAndAfter(any(), any()) } answers { listOf(mockk()) }

          val response = callHandler(GetChatsRequest())
          response.chats?.isEmpty() shouldBe false
        }
      }
  })
