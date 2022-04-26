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

object ApplicationConfig {
  object API {
    const val version = "v1"
    const val endpoint = "api"
  }

  object Database {
    const val jdbcUrl = "jdbc:mysql://localhost:3306/chat_backend"
    const val driverClassName = "com.mysql.cj.jdbc.Driver"
    const val username = "wgu"
    const val password = ""
    const val maximumPoolSize = 3
  }

  object Keys {
    const val secret = "69"
  }

  object JWT {
    const val secret = "secret"
    const val issuer = "http://0.0.0.0:8080"
    const val audience = "http://0.0.0.0:8080/dashboard"
    const val tokenDuration = 3600000 * 24 // 24 hours
  }
}
