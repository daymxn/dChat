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

package com.daymxn.dchat

import com.daymxn.dchat.datamodel.*
import com.daymxn.dchat.service.ActivityService
import com.daymxn.dchat.service.ChatService
import com.daymxn.dchat.service.MessageService
import com.daymxn.dchat.service.UserService
import com.daymxn.dchat.util.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.util.date.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Static object that facilitates communication with service providers for the database.
 *
 * Application logic is kept to a minimum, and should instead reside in route handlers.
 */
object DatabaseManager {
  private val hikariConfig =
    HikariConfig().apply {
      ApplicationConfig.Database.let {
        jdbcUrl = it.jdbcUrl
        driverClassName = it.driverClassName
        username = it.username
        password = it.password
        maximumPoolSize = it.maximumPoolSize
      }
    }

  /** Connects to the database and does some initial init tasks */
  fun connect() {
    Database.connect(HikariDataSource(hikariConfig)).also { createTablesIfAbsent() }
  }

  /** Ensures that tables are always present, as to avoid any potential errors */
  private fun createTablesIfAbsent() {
    transaction {
      addLogger(StdOutSqlLogger)

      arrayOf(UserTable, ChatTable, MessageTable, ActivityTable).forEach { SchemaUtils.create(it) }
    }
  }

  /**
   * Validation method for KTOR to call on when receiving an authentication request.
   *
   * Ensures that the user exists and matches the provided username and password.
   *
   * @param user the [User] to validate with
   *
   * @return a [User] object with data of the validated user
   *
   * @throws ApplicationError if the username or password is incorrect
   */
  suspend fun validateUser(user: User): User =
    UserService.getByUsername(user.username)
      .check {
        if (it == null) validationError("Invalid username")
        if (it.password != user.password) validationError("Invalid password")
      }
      .onSuccess { createActivity(Activity(-1, it.id, ActivityType.USER_LOGGED_IN, currentTime())) }
      .mapError { it.toApplicationError() }
      .getOrThrow()

  private fun currentTime(): Long = System.currentTimeMillis()

  /**
   * Inserts a new [User] into the database
   *
   * @param user the new object to insert
   *
   * @return the newly created [User], with an up-to-date ID
   *
   * @throws ApplicationError if the username is already in use
   */
  suspend fun createUser(user: User): User =
    UserService.insert(user)
      .check { if (it == null) validationError("Could not insert user in database") }
      .mapError { it.toApplicationError("Username already in use") }
      .getOrThrow()

  /**
   * Retrieves a [User] from the database by their ID
   *
   * @param id the ID to filter for
   *
   * @return the [User] found
   *
   * @throws ApplicationError if the user is not found
   */
  suspend fun getUserById(id: Long): User =
    UserService.getById(id)
      .check { if (it == null) validationError("User not found") }
      .mapError { it.toApplicationError() }
      .getOrThrow()

  /**
   * Retrieves all [User] objects in the database
   *
   * @return a list of [User] objects
   */
  @NotCurrentlyInUse suspend fun getAllUsers(): List<User> = UserService.getAll()

  /**
   * Retrieves all [Activity] for the [USER_LOGGED_IN][ActivityType.USER_LOGGED_IN] event, within a
   * specified time frame
   *
   * @param since UNIX epoch specifying the max date and time to retrieve from
   *
   * @return a list of the [Activity] found
   */
  suspend fun getUsersLoggedInActivities(since: Long): List<Activity> =
    ActivityService.getByTypeAndAfter(ActivityType.USER_LOGGED_IN, since)

  /**
   * Retrieves all [Activity] for the [MESSAGE_SENT][ActivityType.MESSAGE_SENT] event, within a
   * specified time frame
   *
   * @param since UNIX epoch specifying the max date and time to retrieve from
   *
   * @return a list of the [Activity] found
   */
  suspend fun getMessagesSentActivities(since: Long): List<Activity> =
    ActivityService.getByTypeAndAfter(ActivityType.MESSAGE_SENT, since)

  /**
   * Inserts a new [Activity] into the database
   *
   * @param activity the new object to insert
   *
   * @return the newly created [Activity], with an up-to-date ID
   *
   * @throws ApplicationError if something goes wrong
   */
  private suspend fun createActivity(activity: Activity): Activity =
    ActivityService.insert(activity)
      .check { if (it == null) validationError("Could not insert activity in database") }
      .mapError { it.toApplicationError() }
      .getOrThrow()

  /**
   * Retrieves all [Chat] for the specified [User], within a specified time frame
   *
   * @param user the user to fetch chats for
   * @param since UNIX epoch specifying the max date and time to retrieve from
   *
   * @return a list of the [Chat] found
   */
  suspend fun getChatsForUserSince(user: User, since: Long = 0L): List<Chat> =
    ChatService.getByUserAndAfter(user.id, since)

  /**
   * Creates a new [Chat] between two [User]
   *
   * @param user the initiating user
   * @param receipt the ID of the user to start a chat with
   *
   * @return the created [Chat]
   *
   * @throws ApplicationError if there already exists a chat between the two users
   */
  suspend fun startChatForUserWith(user: User, receipt: Long): Chat =
    ChatService.insert(
        Chat(
          id = -1,
          owner = user.id,
          receiver = getUserById(receipt).id,
          lastActivity = getTimeMillis()
        )
      )
      .check { if (it == null) validationError("Could not create chat in database") }
      .mapError { it.toApplicationError("You already have a chat opened with that user") }
      .getOrThrow()

  /**
   * Fetches all [User] whose username is a substring of a specified [String]
   *
   * Utilized on the client-side to search for users
   *
   * @param substring what to filter usernames for
   *
   * @return a list of [UserHead] whose username matches the substring
   */
  suspend fun getUsersForSubstring(substring: String): List<UserHead> =
    UserService.getByUsernameLike(substring).map {
      UserHead(id = it.id, username = it.username, isAdmin = it.isAdmin)
    }

  /**
   * Removes a [Chat] from the database
   *
   * Note that [Message] objects linked to the [Chat] will **not** be deleted. The enacting client
   * must also be a part of the [Chat] to delete it.
   *
   * @param user the client wanting to delete the chat
   * @param chat the ID of the chat to delete
   *
   * @return whether the chat was deleted or not
   */
  suspend fun deleteChat(user: User, chat: Long): Boolean =
    ChatService.deleteIfUserApartOf(user.id, chat)

  /**
   * Creates a new [Message] in the database
   *
   * Also calls out to create a new [Activity] for [MESSAGE_SENT][ActivityType.MESSAGE_SENT], if the
   * operation is successful
   *
   * @param message the new object to insert
   *
   * @return the newly created [Message], with an up-to-date ID
   *
   * @throws ApplicationError if something goes wrong
   */
  suspend fun createMessage(message: Message): Message =
    MessageService.insert(message)
      .check { if (it == null) validationError("Could not save message in database") }
      .onSuccess {
        createActivity(Activity(-1, it.sender, ActivityType.MESSAGE_SENT, currentTime()))
      }
      .mapError { it.toApplicationError() }
      .getOrThrow()

  /**
   * Retrieves a [Chat] from the database by ID
   *
   * @param id the ID to filter for
   *
   * @return the [Chat] found
   *
   * @throws ApplicationError if the chat is not found
   */
  suspend fun getChatById(id: Long): Chat =
    ChatService.getById(id)
      .check { if (it == null) validationError("Chat not found") }
      .mapError { it.toApplicationError() }
      .getOrThrow()

  /**
   * Retrieves all [Message] for the specified [Chat], that were sent within a specified time frame
   *
   * @param chat the ID of the [Chat] to filter messages from
   * @param since UNIX epoch specifying the max date and time to retrieve from
   *
   * @return a list of the [Message] found
   */
  suspend fun getMessagesForChatSince(chat: Long, since: Long): List<Message> =
    MessageService.getByChatAndAfter(chat, since)
}
