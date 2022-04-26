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

import com.daymxn.dchat.datamodel.MessageTable.chat
import com.daymxn.dchat.datamodel.MessageTable.content
import com.daymxn.dchat.datamodel.MessageTable.id
import com.daymxn.dchat.datamodel.MessageTable.primaryKey
import com.daymxn.dchat.datamodel.MessageTable.sender
import com.daymxn.dchat.datamodel.MessageTable.sentAt
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

/**
 * Database table schema for [Message]
 *
 * @property id unique [Long] column index, automatically created and incremented for each entry
 * @property sender **indexed** [Long] column foreign key pointing to the [User] that created the
 * entry
 * @property sentAt epoch UNIX [Long] column
 * @property chat **indexed** [Long] column foreign key point to the [Chat] that this entry was
 * created for
 * @property content [String] column containing the message itself, limited to a length of 300
 * characters
 * @property primaryKey primary key constraint pointing to the id property
 */
object MessageTable : Table() {
  val id = long("id").autoIncrement()
  val sender = long("sender_id")
  val sentAt = long("sent_at")
  val chat = long("chat_id").index()
  val content = varchar("username", length = 300)

  override val primaryKey = PrimaryKey(id, name = "pk_message_id")
}

/**
 * Content sent in a [Chat] between [User] objects
 *
 * @property id unique [Long] assigned to each [Message] from the database
 * @property sender the ID of the [User] that sent this [Message]
 * @property sentAt epoch UNIX [Long] representing the creation date of this [Message]
 * @property chat the ID of the [Chat] that this [Message] was created under
 * @property content the actual message that is to be displayed to either [User]
 */
@Serializable
data class Message(
  val id: Long,
  val sender: Long,
  val sentAt: Long,
  val chat: Long,
  val content: String,
) : Datamodel
