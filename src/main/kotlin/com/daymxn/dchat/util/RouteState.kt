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
import com.daymxn.dchat.routes.Request
import com.daymxn.dchat.routes.Response
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlin.reflect.KClass

typealias RouteHandler = suspend RouteState.() -> Unit

typealias ApplicationPipeline = PipelineContext<Unit, ApplicationCall>

class RouteState(val pipeline: ApplicationPipeline) {
  fun getUser(): User? = pipeline.call.principal()

  suspend fun <T : Request> getRequest(clazz: KClass<T>): T = pipeline.call.receive(clazz)

  suspend inline fun <reified T : Request> getRequest(): T = getRequest(T::class)

  suspend fun respond(statusCode: HttpStatusCode, response: Response) {
    pipeline.call.respond(statusCode, response)
  }
}

fun ApplicationPipeline.routeState() = RouteState(this)
