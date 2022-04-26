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

import com.daymxn.dchat.DatabaseManager
import com.daymxn.dchat.datamodel.ActivityType
import com.daymxn.dchat.util.RouteHandler
import com.daymxn.dchat.util.handleApplicationErrors
import com.daymxn.dchat.util.routeState
import io.ktor.http.*
import io.ktor.server.routing.*

fun Route.getActivity() {
  get(GetActivityRoute) { getActivityRouteHandler(routeState()) }
}

val getActivityRouteHandler: RouteHandler = {
  handleApplicationErrors(GetActivityResponse::class) {
    validateRequest(getRequest()).also {
      when (it.activityType) {
        ActivityType.USER_LOGGED_IN -> DatabaseManager.getUsersLoggedInActivities(it.since)
        ActivityType.MESSAGE_SENT -> DatabaseManager.getMessagesSentActivities(it.since)
      }.apply { respond(HttpStatusCode.Accepted, GetActivityResponse(this)) }
    }
  }
}

private fun validateRequest(request: GetActivityRequest): ValidatedGetActivityRequest =
  ValidatedGetActivityRequest(
    validateActivityType(request.activityType),
    validateSince(request.since)
  )

private fun validateActivityType(activityType: ActivityType) = activityType

private fun validateSince(since: ULong) = since.toLong()

private data class ValidatedGetActivityRequest(val activityType: ActivityType, val since: Long)
