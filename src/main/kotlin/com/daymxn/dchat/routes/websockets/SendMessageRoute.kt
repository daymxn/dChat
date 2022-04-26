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
import com.daymxn.dchat.datamodel.Chat
import com.daymxn.dchat.datamodel.Message
import com.daymxn.dchat.datamodel.User
import com.daymxn.dchat.routes.SendMessageRequest
import com.daymxn.dchat.routes.SendMessageResponse
import com.daymxn.dchat.util.WebSocketConnection
import com.daymxn.dchat.util.WebSocketConnectionManager
import com.daymxn.dchat.util.validationError
import io.ktor.util.date.*

suspend fun WebSocketConnection.sendMessageRoute(request: SendMessageRequest) {
  validateRequest(request, user).also { validatedRequest ->
    validateUserIsAPartOfChat(validatedRequest.message.chat, user).also { validatedChat ->
      DatabaseManager.createMessage(validatedRequest.message).also {
        session.sendMessage(SendMessageResponse())
        WebSocketConnectionManager.notifyClient(whichClientIsNotMe(validatedChat, user.id), request)
      }
    }
  }
}

private fun whichClientIsNotMe(chat: Chat, user: Long): Long =
  if (chat.owner == user) chat.receiver else chat.owner

private suspend fun validateUserIsAPartOfChat(chat: Long, user: User) =
  DatabaseManager.getChatById(chat).also {
    if (it.owner != user.id && it.receiver != user.id)
      validationError("You are not apart of this chat")
  }

private fun validateRequest(request: SendMessageRequest, user: User): ValidatedSendMessageRequest =
  ValidatedSendMessageRequest(
    Message(
      -1,
      user.id,
      getTimeMillis(),
      validateChat(request.chat),
      validateMessage(request.message)
    )
  )

private fun validateChat(chat: ULong): Long = chat.toLong()

private fun validateMessage(message: String): String =
  message.ifBlank { validationError("Message can not be blank") }

private data class ValidatedSendMessageRequest(val message: Message)
