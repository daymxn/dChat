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

import com.daymxn.dchat.routes.GetUsersForSubstringRequest
import com.daymxn.dchat.routes.GetUsersForSubstringResponse
import com.daymxn.dchat.service.UserService
import com.daymxn.dchat.util.GenerateMockObjects
import com.daymxn.dchat.util.MockDatabaseService
import com.daymxn.dchat.util.WebSocketTestManager
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery

class GetUsersForSubstringRouteTest :
  FreeSpec({
    val testManager = WebSocketTestManager()

    MockDatabaseService().enable()

    suspend fun callHandler(request: GetUsersForSubstringRequest): GetUsersForSubstringResponse =
      testManager.invokeHandler(request).shouldBeInstanceOf()

    "request validation" -
      {
        "fails on less than 3 chars" {
          arrayOf("12", "1", "").forEach {
            val response = callHandler(GetUsersForSubstringRequest(it))
            response.error shouldNotBe null
          }
        }
        "removes non alphanumeric characters" {
          val response = callHandler(GetUsersForSubstringRequest("__b__!+;'[]2"))
          response.error shouldNotBe null
        }
        "fails on internal database error" {
          coEvery { UserService.getByUsernameLike(any()) } throws Exception()

          val response = callHandler(GetUsersForSubstringRequest("hello world!"))
          response.error shouldNotBe null
        }
      }

    "works as expected" {
      coEvery { UserService.getByUsernameLike(any()) } answers
        {
          listOf(GenerateMockObjects.user())
        }

      val response = callHandler(GetUsersForSubstringRequest("hello world!"))
      response.users?.isEmpty() shouldBe false
    }
  })
