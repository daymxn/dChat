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

import com.daymxn.dchat.datamodel.ActivityTable.id
import com.daymxn.dchat.datamodel.ActivityTable.owner
import com.daymxn.dchat.datamodel.ActivityTable.primaryKey
import com.daymxn.dchat.datamodel.ActivityTable.timestamp
import com.daymxn.dchat.datamodel.ActivityTable.type
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

/**
 * Database table schema for [Activity]
 *
 * @property id unique [Long] column index, automatically created and incremented for each entry
 * @property owner **indexed** [Long] column foreign key pointing to the [User] that created the
 * entry
 * @property type enum for [ActivityType]
 * @property timestamp epoch UNIX [Long] column
 * @property primaryKey primary key constraint pointing to the id property
 */
object ActivityTable : Table() {
  val id = long("id").autoIncrement()
  val owner = long("owner_id").index()
  val type = enumeration("type", ActivityType::class)
  val timestamp = long("timestamp")

  override val primaryKey = PrimaryKey(id, name = "pk_activity_id")
}

/**
 * Loggable actions committed by [User]
 *
 * Activities are the primary source of data collection for the application. Take a look at
 * [ActivityType] to get an idea of all the possible data points.
 *
 * @property id unique [Long] assigned to each [Activity] from the database
 * @property owner the ID of the [User] the committed the action that created this [Activity]
 * @property type the specific [ActivityType] that this [Activity] represents
 * @property timestamp epoch UNIX [Long] representing the creation date of this [Activity]
 */
@Serializable
data class Activity(
  val id: Long,
  val owner: Long,
  val type: ActivityType,
  val timestamp: Long,
) : Datamodel

/** Enum representations of all possible data points to be collected for [Activity] creation */
@Serializable
enum class ActivityType {

  /** Represents a successful authentication for a [User] */
  USER_LOGGED_IN,

  /** Represents a successful [Message] creation for a [User] in a [Chat] */
  MESSAGE_SENT
}
