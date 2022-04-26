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

import com.daymxn.dchat.util.ApplicationConfig

const val COMMON_ROUTE = "/${ApplicationConfig.API.endpoint}/${ApplicationConfig.API.version}"

const val COMMON_DASHBOARD = "$COMMON_ROUTE/dashboard"

const val LoginRoute = "$COMMON_ROUTE/login"

const val RegisterRoute = "$COMMON_ROUTE/register"

const val HealthRoute = "$COMMON_ROUTE/health"

const val GetActivityRoute = "$COMMON_DASHBOARD/getActivity"

const val GetChatsRoute = "$COMMON_DASHBOARD/getChats"

const val StartChatRoute = "$COMMON_DASHBOARD/startChat"

const val DeleteChatRoute = "$COMMON_DASHBOARD/deleteChat"

const val WebSocketMainRoute = "$COMMON_DASHBOARD/ws"
