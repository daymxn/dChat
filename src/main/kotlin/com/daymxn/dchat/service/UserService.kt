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

import com.daymxn.dchat.datamodel.User
import com.daymxn.dchat.datamodel.UserTable
import com.daymxn.dchat.util.NotCurrentlyInUse
import com.daymxn.dchat.util.newQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/** Static service provider that facilitates communication with [UserTable] */
object UserService {

  /**
   * Fetches all [User] from the database
   *
   * @return a [List] of all the [User] found
   */
  suspend fun getAll(): List<User> = newSuspendedTransaction {
    UserTable.selectAll().mapNotNull { it.toUser() }
  }

  /**
   * Fetches a [User] from the database, by the ID
   *
   * @param id the ID to fetch a [User] for
   *
   * @return a [Result] of either the [User] if found, null if not found, or failed if an exception
   * occurred
   */
  suspend fun getById(id: Long): Result<User?> = newQuery {
    UserTable.select { UserTable.id eq id }.firstNotNullOfOrNull { it.toUser() }
  }

  /**
   * Fetches all [User] from the database whose username contains a specified [String]
   *
   * This lives in the Service layer as to mitigate the complications that would arise out of
   * fetching the entire [User] database for each search
   *
   * @param str a [String] to filter usernames against
   *
   * @return a list [User] that match the specified filters
   */
  suspend fun getByUsernameLike(str: String): List<User> = newSuspendedTransaction {
    UserTable.select { UserTable.username like "%$str%" }.mapNotNull { it.toUser() }
  }

  /**
   * Fetches a [User] from the database, by the username
   *
   * Primarily utilized in authentication.
   *
   * @param username the ID to fetch a [User] for
   *
   * @return a [Result] of either the [User] if found, null if not found, or failed if an exception
   * occurred
   */
  suspend fun getByUsername(username: String): Result<User?> = newQuery {
    UserTable.select { UserTable.username eq username }.firstNotNullOfOrNull { it.toUser() }
  }

  /**
   * Updates a [User] in the database with new data
   *
   * @param user the new object to update with
   *
   * @return whether the update was successful or not
   */
  @NotCurrentlyInUse
  suspend fun update(user: User): Boolean =
    newSuspendedTransaction {
      UserTable.update({ UserTable.id eq user.id }) {
        it[username] = user.username
        it[password] = user.password
      }
    } == 1

  /**
   * Inserts a [User] into the database
   *
   * @param user the new object to insert
   *
   * @return a [Result] of either the new [User] if successful, null if something went wrong, or
   * failed if an exception occurred
   */
  suspend fun insert(user: User): Result<User?> = newQuery {
    UserTable.insert {
      it[username] = user.username
      it[password] = user.password
    }
      .resultedValues
      ?.firstNotNullOfOrNull { it.toUser() }
  }

  /** Helper method to convert a retrieved [ResultRow] from the database to a [User] */
  private fun ResultRow.toUser(): User =
    User(
      id = this[UserTable.id],
      username = this[UserTable.username],
      password = this[UserTable.password],
      isAdmin = this[UserTable.isAdmin]
    )
}
