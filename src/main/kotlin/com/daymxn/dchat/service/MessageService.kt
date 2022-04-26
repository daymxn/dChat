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

package com.daymxn.dchat.service

import com.daymxn.dchat.datamodel.Chat
import com.daymxn.dchat.datamodel.Message
import com.daymxn.dchat.datamodel.MessageTable
import com.daymxn.dchat.util.NotCurrentlyInUse
import com.daymxn.dchat.util.newQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/** Static service provider that facilitates communication with [MessageTable] */
object MessageService {

  /**
   * Fetches all [Message] from the database
   *
   * @return a [List] of all the [Message] found
   */
  @NotCurrentlyInUse
  suspend fun getAll(): List<Message> = newSuspendedTransaction {
    MessageTable.selectAll().mapNotNull { it.toMessage() }
  }

  /**
   * Fetches a [Message] from the database, by the ID
   *
   * @param id the ID to fetch a [Message] for
   *
   * @return a [Result] of either the [Message] if found, null if not found, or failed if an
   * exception occurred
   */
  @NotCurrentlyInUse
  suspend fun getById(id: Long): Result<Message?> = newQuery {
    MessageTable.select { MessageTable.id eq id }.firstNotNullOfOrNull { it.toMessage() }
  }

  /**
   * Fetches all [Message] from the database that are used by a specified [Chat] and are younger
   * than a specified UNIX timestamp.
   *
   * @param chat the ID of the [Chat] to filter for
   * @param since a UNIX timestamp specifying the oldest [Message] to fetch
   *
   * @return a list [Message] that match the specified filters
   */
  suspend fun getByChatAndAfter(chat: Long, since: Long): List<Message> = newSuspendedTransaction {
    MessageTable.select {
      MessageTable.chat eq chat
      MessageTable.sentAt greaterEq since
    }
      .mapNotNull { it.toMessage() }
  }

  /**
   * Removes a [Message] from the database
   *
   * @param id ID of the message to remove
   *
   * @return whether the removal was successful or not
   */
  @NotCurrentlyInUse
  suspend fun delete(id: Long): Boolean = newSuspendedTransaction {
    MessageTable.deleteWhere { MessageTable.id eq id } == 1
  }

  /**
   * Updates a [Message] in the database with new data
   *
   * @param message the new object to update with
   *
   * @return whether the update was successful or not
   */
  @NotCurrentlyInUse
  suspend fun update(message: Message): Boolean =
    newSuspendedTransaction {
      MessageTable.update({ MessageTable.id eq message.id }) { it[content] = message.content }
    } == 1

  /**
   * Inserts a [Message] into the database
   *
   * @param message the new object to insert
   *
   * @return a [Result] of either the new [Message] if successful, null if something went wrong, or
   * failed if an exception occurred
   */
  suspend fun insert(message: Message): Result<Message?> = newQuery {
    MessageTable.insert {
      it[sender] = message.sender
      it[sentAt] = message.sentAt
      it[chat] = message.chat
      it[content] = message.content
    }
      .resultedValues
      ?.firstNotNullOfOrNull { it.toMessage() }
  }

  /** Helper method to convert a retrieved [ResultRow] from the database to a [Message] */
  private fun ResultRow.toMessage(): Message =
    Message(
      id = this[MessageTable.id],
      sender = this[MessageTable.sender],
      sentAt = this[MessageTable.sentAt],
      chat = this[MessageTable.chat],
      content = this[MessageTable.content]
    )
}
