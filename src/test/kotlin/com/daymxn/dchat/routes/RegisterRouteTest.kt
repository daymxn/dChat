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

import com.daymxn.dchat.datamodel.User
import com.daymxn.dchat.service.UserService
import com.daymxn.dchat.util.*
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk

class RegisterRouteTest :
  FreeSpec({
    val testManager = ApplicationTestManager(registerRouteHandler)
    MockDatabaseService().enable()

    suspend fun callHandler(request: RegisterRequest): RegisterResponse =
      testManager.invokeHandler(request).shouldBeInstanceOf()

    "request validation" -
      {
        "fails when missing username" {
          val response = callHandler(RegisterRequest("", USER_PASSWORD_TESTS))
          response.error shouldNotBe null
        }
        "fails when missing password" {
          val response = callHandler(RegisterRequest(USER_USERNAME_TESTS, ""))
          response.error shouldNotBe null
        }
        "fails when username is already in use" {
          coEvery { UserService.insert(any()) } answers { ColumnAlreadyExists(mockk()).asFailure() }

          val response = callHandler(RegisterRequest(USER_USERNAME_TESTS, USER_PASSWORD_TESTS))
          response.error shouldNotBe null
        }
        "fails on internal database error" {
          coEvery { UserService.insert(any()) } answers { null.asSuccess() }

          val response = callHandler(RegisterRequest(USER_USERNAME_TESTS, USER_PASSWORD_TESTS))
          response.error shouldNotBe null
        }
      }

    "works as expected" {
      coEvery { UserService.insert(any()) } answers { firstArg<User>().asSuccess() }

      val response = callHandler(RegisterRequest(USER_USERNAME_TESTS, USER_PASSWORD_TESTS))
      response.accessToken shouldNotBe null
    }
  })
