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

import com.daymxn.dchat.DatabaseManager
import com.daymxn.dchat.routes.GetUsersForSubstringRequest
import com.daymxn.dchat.routes.GetUsersForSubstringResponse
import com.daymxn.dchat.util.WebSocketConnection
import com.daymxn.dchat.util.validationError

suspend fun WebSocketConnection.getUsersForSubstringRoute(request: GetUsersForSubstringRequest) {
  validateRequest(request).also { validatedRequest ->
    DatabaseManager.getUsersForSubstring(validatedRequest.search).also {
      session.sendMessage(GetUsersForSubstringResponse(it))
    }
  }
}

private fun validateRequest(
  request: GetUsersForSubstringRequest
): ValidatedGetUsersForSubstringRequest =
  ValidatedGetUsersForSubstringRequest(validateSearch(request.search))

private fun validateSearch(search: String): String =
  search.filter { it.isLetterOrDigit() }.also {
    if (it.length < 3)
      validationError("Search query can not be less than 3 alphanumeric characters.")
  }

private data class ValidatedGetUsersForSubstringRequest(val search: String)
