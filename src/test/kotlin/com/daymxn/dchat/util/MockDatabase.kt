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

import com.daymxn.dchat.service.ActivityService
import com.daymxn.dchat.service.ChatService
import com.daymxn.dchat.service.MessageService
import com.daymxn.dchat.service.UserService
import io.mockk.mockkObject
import io.mockk.unmockkObject

internal class MockDatabaseService {
  fun enable() {
    mockkObject(ActivityService, ChatService, MessageService, UserService)
  }

  fun disable() {
    unmockkObject(ActivityService, ChatService, MessageService, UserService)
  }
}