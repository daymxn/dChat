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

package com.daymxn.dchat.datamodel

import com.daymxn.dchat.datamodel.UserTable.id
import com.daymxn.dchat.datamodel.UserTable.isAdmin
import com.daymxn.dchat.datamodel.UserTable.password
import com.daymxn.dchat.datamodel.UserTable.primaryKey
import com.daymxn.dchat.datamodel.UserTable.username
import io.ktor.server.auth.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

/**
 * Database table schema for [User]
 *
 * @property id unique [Long] column index, automatically created and incremented for each entry
 * @property username unique **indexed* [String] column provided for each entry
 * @property password secret [String] column provided for each entry
 * @property isAdmin [Boolean] column that defaults to false
 * @property primaryKey primary key constraint pointing to the id property
 */
object UserTable : Table() {
  val id = long("id").autoIncrement()
  val username = varchar("username", length = 50).uniqueIndex()
  val password = varchar("password", length = 50)
  val isAdmin = bool("is_admin").default(false)

  override val primaryKey = PrimaryKey(id, name = "pk_user_id")
}

/**
 * An authenticated end-user for the application
 *
 * This differs from what the client may consume when fetching other users outside of
 * authentication. For such scenarios, take a look at [UserHead],
 *
 * @property id unique [Long] assigned to each [Chat] from the database
 * @property username unique [String] created by each end-user
 * @property password hashed [String] representing the secret chosen by the end-user
 * @property isAdmin [Boolean] that signifies if a [User] is and administrator of the application.
 * Utilized for higher privilege actions.
 */
@Serializable
data class User(
  val id: Long,
  val username: String,
  val password: String,
  val isAdmin: Boolean = false,
) : Datamodel, Principal

/**
 * [User] object retrieved from the database, with sensitive data removed
 *
 * Especially useful in scenarios where end-users need [User] information, but should not have
 * access to sensitive data stored for each entry.
 *
 * @property id unique [Long] assigned to each [Chat] from the database
 * @property username unique [String] created by each end-user
 * @property isAdmin [Boolean] that signifies if a [User] is and administrator of the application.
 * Utilized for higher privilege actions.
 */
@Serializable
data class UserHead(
  val id: Long,
  val username: String,
  val isAdmin: Boolean,
) : Datamodel
