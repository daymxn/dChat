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

import com.daymxn.dchat.DatabaseManager
import com.daymxn.dchat.datamodel.User
import com.daymxn.dchat.util.*
import io.ktor.http.*
import io.ktor.server.routing.*

fun Route.login() {
  post(LoginRoute) { loginRouteHandler(routeState()) }
}

val loginRouteHandler: RouteHandler = {
  handleApplicationErrors(LoginResponse::class) {
    validateRequest(getRequest()).also {
      DatabaseManager.validateUser(it.user).apply {
        respond(HttpStatusCode.Accepted, LoginResponse(JWTManager.createToken(this)))
      }
    }
  }
}

private fun validateRequest(request: LoginRequest): ValidatedLoginRequest =
  ValidatedLoginRequest(
    User(
      -1,
      validateUsername(request.username),
      JWTManager.hashPassword(validatePassword(request.password))
    )
  )

private fun validateUsername(username: String) =
  username.takeIf { it.isNotBlank() } ?: validationError("Username is a required field")

private fun validatePassword(password: String) =
  password.takeIf { it.isNotBlank() } ?: validationError("Password is a required field")

private data class ValidatedLoginRequest(val user: User)
