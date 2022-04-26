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

import com.daymxn.dchat.datamodel.ChatTable.id
import com.daymxn.dchat.datamodel.ChatTable.lastActivity
import com.daymxn.dchat.datamodel.ChatTable.owner
import com.daymxn.dchat.datamodel.ChatTable.primaryKey
import com.daymxn.dchat.datamodel.ChatTable.receiver
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

/**
 * Database table schema for [Chat]
 *
 * Note that there exists a composite index against [owner] and [receiver]
 *
 * @property id unique [Long] column index, automatically created and incremented for each entry
 * @property owner **indexed** [Long] column foreign key pointing to the [User] that created the
 * entry
 * @property receiver **indexed** [Long] column foreign key pointing to the [User] that was sent
 * this entry
 * @property lastActivity epoch UNIX [Long] column
 * @property primaryKey primary key constraint pointing to the id property
 */
object ChatTable : Table() {
  val id = long("id").autoIncrement()
  val owner = long("owner_id").index()
  val receiver = long("receiver_id").index()
  val lastActivity = long("last_activity")

  override val primaryKey = PrimaryKey(id, name = "pk_chat_id")

  init {
    uniqueIndex("owner_and_receiver", owner, receiver)
  }
}

/**
 * Rooms created to facilitate the creation and retrieval of [Message] objects between [User]
 * objects
 *
 * While chats are currently limited to two [User] objects, this can be expanded in the future to
 * support groups of [User] objects.
 *
 * Keep in mind that all [Message] point to a given [Chat].
 *
 * @property id unique [Long] assigned to each [Chat] from the database
 * @property owner the ID of the [User] that started this [Chat]
 * @property receiver the ID of the [User] that this [Chat] was started with
 * @property lastActivity epoch UNIX [Long] representing the last interaction with this [Chat] (eg;
 * creation or messages sent)
 */
@Serializable
data class Chat(
  val id: Long,
  val owner: Long,
  val receiver: Long,
  val lastActivity: Long,
) : Datamodel
