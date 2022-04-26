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

const val USER_USERNAME_TESTS = "username"
const val USER_PASSWORD_TESTS = "p@ssw0rd!"

const val LOCAL_USER_USERNAME_TESTS = "user"
const val LOCAL_USER_PASSWORD_TESTS = USER_PASSWORD_TESTS

internal object GenerateMockObjects {
  fun user(id: Long = 1): User = User(id, USER_USERNAME_TESTS, USER_PASSWORD_TESTS)

  fun localUser(): User = User(0, LOCAL_USER_USERNAME_TESTS, LOCAL_USER_PASSWORD_TESTS)
}
