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

import com.daymxn.dchat.datamodel.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface EndpointHeader

sealed interface Request : EndpointHeader

sealed interface Response : EndpointHeader

sealed interface WebSocketHeader

@Serializable sealed class WebSocketRequest : WebSocketHeader, Request

@Serializable sealed class WebSocketResponse : WebSocketHeader, Response

@Serializable data class LoginRequest(val username: String, val password: String) : Request

@Serializable
data class LoginResponse(val accessToken: String? = null, val error: String? = null) : Response

@Serializable data class RegisterRequest(val username: String, val password: String) : Request

@Serializable
data class RegisterResponse(val accessToken: String? = null, val error: String? = null) : Response

@Serializable
data class GetActivityRequest(val activityType: ActivityType, val since: ULong = 0u) : Request

@Serializable
data class GetActivityResponse(val activities: List<Activity>? = null, val error: String? = null) :
  Response

@Serializable data class GetChatsRequest(val since: ULong = 0u) : Request

@Serializable
data class GetChatsResponse(val chats: List<Chat>? = null, val error: String? = null) : Response

@Serializable data class StartChatRequest(val receipt: ULong) : Request

@Serializable
data class StartChatResponse(val chat: Chat? = null, val error: String? = null) : Response

@Serializable data class DeleteChatRequest(val chat: ULong) : Request

@Serializable data class DeleteChatResponse(val error: String? = null) : Response

@Serializable
@SerialName("GetUsersForSubstringRequest")
data class GetUsersForSubstringRequest(val search: String) : WebSocketRequest()

@Serializable
@SerialName("GetUsersForSubstringResponse")
data class GetUsersForSubstringResponse(
  val users: List<UserHead>? = null,
  val error: String? = null
) : WebSocketResponse()

@Serializable
@SerialName("SendMessageRequest")
data class SendMessageRequest(val chat: ULong, val message: String) : WebSocketRequest()

@Serializable
@SerialName("SendMessageResponse")
data class SendMessageResponse(val error: String? = null) : WebSocketResponse()

@Serializable
@SerialName("GetChatHistoryRequest")
data class GetChatHistoryRequest(val chat: ULong, val since: ULong) : WebSocketRequest()

@Serializable
@SerialName("GetChatHistoryResponse")
data class GetChatHistoryResponse(val messages: List<Message>? = null, val error: String? = null) :
  WebSocketResponse()
