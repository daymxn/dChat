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
import com.daymxn.dchat.datamodel.User
import com.daymxn.dchat.routes.GetChatHistoryRequest
import com.daymxn.dchat.routes.GetChatHistoryResponse
import com.daymxn.dchat.util.WebSocketConnection
import com.daymxn.dchat.util.validationError

suspend fun WebSocketConnection.getChatHistoryRoute(request: GetChatHistoryRequest) {
  validateRequest(request, user).also { validatedRequest ->
    validateUserIsAPartOfChat(validatedRequest.chat, user).also {
      DatabaseManager.getMessagesForChatSince(validatedRequest.chat, validatedRequest.since).also {
        session.sendMessage(GetChatHistoryResponse(it))
      }
    }
  }
}

private suspend fun validateUserIsAPartOfChat(chat: Long, user: User) {
  DatabaseManager.getChatById(chat).also {
    if (it.owner != user.id && it.receiver != user.id)
      validationError("You are not apart of this chat")
  }
}

private fun validateRequest(
  request: GetChatHistoryRequest,
  user: User
): ValidatedGetChatHistoryRequest =
  ValidatedGetChatHistoryRequest(validateChat(request.chat), validateSince(request.since))

private fun validateChat(chat: ULong): Long = chat.toLong()

private fun validateSince(since: ULong): Long = since.toLong()

private data class ValidatedGetChatHistoryRequest(val chat: Long, val since: Long)
