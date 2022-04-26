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

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.daymxn.dchat.datamodel.User
import io.ktor.util.*
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** Static object that facilitates common [JWT] related operations */
object JWTManager {

  /**
   * Creates a new [JWT] token for the specified [User]
   *
   * @param user the user to create a token for
   *
   * @return a [String] representation of the token created
   */
  fun createToken(user: User): String =
    JWT
      .create()
      .withAudience(ApplicationConfig.JWT.audience)
      .withIssuer(ApplicationConfig.JWT.issuer)
      .withClaim("id", user.id)
      .withExpiresAt(Date(System.currentTimeMillis() + ApplicationConfig.JWT.tokenDuration))
      .sign(Algorithm.HMAC256(ApplicationConfig.JWT.secret))

  /**
   * Hashes a [String] with application-specific hashing for passwords
   *
   * @param password the raw [String] to create a hash for
   *
   * @return a [String] representation of the hash created from the specified password
   */
  fun hashPassword(password: String): String {
    Mac.getInstance("HmacSHA1").run {
      this.init(SecretKeySpec(hex(ApplicationConfig.Keys.secret), "HmacSHA1"))
      return hex(this.doFinal(password.toByteArray(Charsets.UTF_8)))
    }
  }

  fun User.hashPassword(): User = this.copy(password = JWTManager.hashPassword(this.password))
}
