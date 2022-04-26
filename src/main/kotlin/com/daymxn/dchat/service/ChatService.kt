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
import com.daymxn.dchat.datamodel.ChatTable
import com.daymxn.dchat.datamodel.User
import com.daymxn.dchat.util.NotCurrentlyInUse
import com.daymxn.dchat.util.newQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/** Static service provider that facilitates communication with [ChatTable] */
object ChatService {

  /**
   * Fetches all [Chat] from the database
   *
   * @return a [List] of all the [Chat] found
   */
  @NotCurrentlyInUse
  suspend fun getAll(): List<Chat> = newSuspendedTransaction {
    ChatTable.selectAll().mapNotNull { it.toChat() }
  }

  /**
   * Fetches a [Chat] from the database, by the ID
   *
   * @param id the ID to fetch a [Chat] for
   *
   * @return a [Result] of either the [Chat] if found, null if not found, or failed if an exception
   * occurred
   */
  @NotCurrentlyInUse
  suspend fun getById(id: Long): Result<Chat?> = newQuery {
    ChatTable.select { ChatTable.id eq id }.firstNotNullOfOrNull { it.toChat() }
  }

  /**
   * Fetches all [Chat] from the database that are used by a specified [User] and are younger than a
   * specified UNIX timestamp.
   *
   * @param id the ID of the [User] to filter for
   * @param since a UNIX timestamp specifying the oldest [Chat] to fetch
   *
   * @return a list [Chat] that match the specified filters
   */
  suspend fun getByUserAndAfter(id: Long, since: Long): List<Chat> = newSuspendedTransaction {
    ChatTable.select {
      (ChatTable.owner eq id) or (ChatTable.receiver eq id)
      ChatTable.lastActivity greaterEq since
    }
      .mapNotNull { it.toChat() }
  }

  /**
   * Fetches all [Chat] from the database that were **created** by a specified [User] and are
   * younger than a specified UNIX timestamp.
   *
   * @param owner the ID of the [User] to filter for
   * @param since a UNIX timestamp specifying the oldest [Chat] to fetch
   *
   * @return a list [Chat] that match the specified filters
   */
  @NotCurrentlyInUse
  suspend fun getByOwnerAndAfter(owner: Long, since: Long): List<Chat> = newSuspendedTransaction {
    ChatTable.select {
      ChatTable.owner eq owner
      ChatTable.lastActivity greaterEq since
    }
      .mapNotNull { it.toChat() }
  }

  /**
   * Fetches all [Chat] from the database that were created by a specified user
   *
   * @param owner ID of the user to filter for
   *
   * @return a list of [Chat] that were created by the specified owner
   */
  @NotCurrentlyInUse
  suspend fun getByOwner(owner: Long): List<Chat> = newSuspendedTransaction {
    ChatTable.select { ChatTable.owner eq owner }.mapNotNull { it.toChat() }
  }

  /**
   * Removes a [Chat] from the database, first validating that the specified [User] is a part of the
   * [Chat]
   *
   * Moving this to the service layer allows us to make the filter a part of the SQL query, which is
   * much faster.
   *
   * @param user ID of the user to validate against
   * @param id ID of the chat to remove
   *
   * @return whether the removal was successful or not
   */
  suspend fun deleteIfUserApartOf(user: Long, id: Long): Boolean = newSuspendedTransaction {
    ChatTable.deleteWhere {
      (ChatTable.owner eq user) or (ChatTable.receiver eq user)
      ChatTable.id eq id
    } == 1
  }

  /**
   * Updates a [Chat] in the database with new data
   *
   * @param chat the new object to update with
   *
   * @return whether the update was successful or not
   */
  @NotCurrentlyInUse
  suspend fun update(chat: Chat): Boolean =
    newSuspendedTransaction {
      ChatTable.update({ ChatTable.id eq chat.id }) {
        it[owner] = chat.owner
        it[receiver] = chat.receiver
        it[lastActivity] = chat.lastActivity
      }
    } == 1

  /**
   * Inserts a [Chat] into the database
   *
   * @param chat the new object to insert
   *
   * @return a [Result] of either the new [Chat] if successful, null if something went wrong, or
   * failed if an exception occurred
   */
  suspend fun insert(chat: Chat): Result<Chat?> = newQuery {
    ChatTable.insert {
      it[owner] = chat.owner
      it[receiver] = chat.receiver
      it[lastActivity] = chat.lastActivity
    }
      .resultedValues
      ?.firstNotNullOfOrNull { it.toChat() }
  }

  /** Helper method to convert a retrieved [ResultRow] from the database to a [Chat] */
  private fun ResultRow.toChat(): Chat =
    Chat(
      id = this[ChatTable.id],
      owner = this[ChatTable.owner],
      receiver = this[ChatTable.receiver],
      lastActivity = this[ChatTable.lastActivity]
    )
}
