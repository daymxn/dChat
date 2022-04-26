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
import com.daymxn.dchat.routes.Request
import com.daymxn.dchat.routes.Response
import com.daymxn.dchat.service.UserService
import com.daymxn.dchat.util.JWTManager.hashPassword
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot

class ApplicationTestManager(val routeHandler: RouteHandler) {
  val routeState = mockRouteState()

  suspend inline fun <reified T : Request> invokeHandler(request: T): Response? {
    coEvery { routeState.getRequest(T::class) } returns request

    val response = slot<Response>()
    coEvery { routeState.respond(any(), capture(response)) } answers {}

    routeHandler.invoke(routeState)

    return if (response.isCaptured) response.captured else null
  }

  fun setUserIsLoggedIn(isLoggedIn: Boolean = true) {
    with(GenerateMockObjects.localUser()) {
      if (isLoggedIn) {
        coEvery { routeState.getUser() } returns this
        saveUser(this)
      } else {
        coEvery { routeState.getUser() } returns null
        markUserAsNotFound(this)
      }
    }
  }

  fun saveUser(user: User, id: Long = user.id, username: String = user.username) {
    coEvery { UserService.getById(id) } returns user.hashPassword().asSuccess()
    coEvery { UserService.getByUsername(username) } returns user.hashPassword().asSuccess()
  }

  fun markUserAsNotFound(user: User) {
    coEvery { UserService.getById(user.id) } returns null.asSuccess()
    coEvery { UserService.getByUsername(user.username) } returns null.asSuccess()
  }
}

fun mockRouteState(): RouteState = mockk {}
