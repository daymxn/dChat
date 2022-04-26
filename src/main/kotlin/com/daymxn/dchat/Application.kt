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

package com.daymxn

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.daymxn.dchat.DatabaseManager
import com.daymxn.dchat.routes.*
import com.daymxn.dchat.routes.websockets.webSocketMain
import com.daymxn.dchat.util.ApplicationConfig
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json

/** Entry point for the application */
fun main() {
  DatabaseManager.connect()
  embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
      configureSerialization()
      configureAuthentication()
      configureRouting()
    }
    .start(wait = true)
}

/** Configuration for hooking up all the routing with KTOR */
fun Application.configureRouting() {
  install(Resources)
  routing {
    health()
    login()
    register()
    authenticate("auth-jwt") {
      getChatsForUser()
      startChat()
      deleteChat()
      webSocketMain()
    }
    authenticate("auth-jwt-admin") { getActivity() }
  }
}

/** Configuration for authentication methods with KTOR */
fun Application.configureAuthentication() {
  authentication {
    jwt("auth-jwt") {
      verifier(
        JWT
          .require(Algorithm.HMAC256(ApplicationConfig.JWT.secret))
          .withAudience(ApplicationConfig.JWT.audience)
          .withIssuer(ApplicationConfig.JWT.issuer)
          .build()
      )
      validate {
        it.payload
          .getClaim("id")
          .asLong()
          .runCatching { DatabaseManager.getUserById(this) }
          .getOrNull()
      }
    }

    jwt("auth-jwt-admin") {
      verifier(
        JWT
          .require(Algorithm.HMAC256(ApplicationConfig.JWT.secret))
          .withAudience(ApplicationConfig.JWT.audience)
          .withIssuer(ApplicationConfig.JWT.issuer)
          .build()
      )

      validate {
        it.payload
          .getClaim("id")
          .asLong()
          .runCatching { DatabaseManager.getUserById(this) }
          .getOrNull()
          ?.takeIf { it.isAdmin }
      }
    }
  }
}

/** Configuration for JSON serialization with [EndpointHeader] and [WebSocketHeader] messages */
fun Application.configureSerialization() {
  install(ContentNegotiation) { json() }
  install(WebSockets) { contentConverter = KotlinxWebsocketSerializationConverter(Json) }
}
