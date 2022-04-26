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

import com.daymxn.dchat.datamodel.Activity
import com.daymxn.dchat.datamodel.ActivityTable
import com.daymxn.dchat.datamodel.ActivityType
import com.daymxn.dchat.util.NotCurrentlyInUse
import com.daymxn.dchat.util.newQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/** Static service provider that facilitates communication with [ActivityTable] */
object ActivityService {

  /**
   * Fetches all [Activity] from the database
   *
   * @return a [List] of all the [Activity] found
   */
  @NotCurrentlyInUse
  suspend fun getAll(): List<Activity> = newSuspendedTransaction {
    ActivityTable.selectAll().mapNotNull { it.toActivity() }
  }

  /**
   * Fetches an [Activity] from the database, by the ID
   *
   * @param id the ID to fetch an [Activity] for
   *
   * @return a [Result] of either the [Activity] if found, null if not found, or failed if an
   * exception occurred
   */
  @NotCurrentlyInUse
  suspend fun getById(id: Long): Result<Activity?> = newQuery {
    ActivityTable.select { ActivityTable.id eq id }.firstNotNullOfOrNull { it.toActivity() }
  }

  /**
   * Fetches all [Activity] from the database that match an [ActivityType] and are younger than a
   * specified UNIX timestamp.
   *
   * @param type the [ActivityType] to filter for
   * @param since a UNIX timestamp specifying the oldest [Activity] to fetch
   *
   * @return a list [Activity] that match the specified filters
   */
  suspend fun getByTypeAndAfter(type: ActivityType, since: Long): List<Activity> =
    newSuspendedTransaction {
      ActivityTable.select {
        ActivityTable.type eq type
        ActivityTable.timestamp greaterEq since
      }
        .mapNotNull { it.toActivity() }
    }

  /**
   * Fetches all [Activity] from the database that were created by a specified user
   *
   * @param owner ID of the user to filter for
   *
   * @return a list of [Activity] that were created by the specified owner
   */
  @NotCurrentlyInUse
  suspend fun getByOwner(owner: Long): List<Activity> = newSuspendedTransaction {
    ActivityTable.select { ActivityTable.owner eq owner }.mapNotNull { it.toActivity() }
  }

  /**
   * Updates an [Activity] in the database with new data
   *
   * @param activity the new object to update with
   *
   * @return whether the update was successful or not
   */
  @NotCurrentlyInUse
  suspend fun update(activity: Activity): Boolean =
    newSuspendedTransaction {
      ActivityTable.update({ ActivityTable.id eq activity.id }) {
        it[owner] = activity.owner
        it[type] = activity.type
        it[timestamp] = activity.timestamp
      }
    } == 1

  /**
   * Inserts an [Activity] into the database
   *
   * @param activity the new object to insert
   *
   * @return a [Result] of either the new [Activity] if successful, null if something went wrong, or
   * failed if an exception occurred
   */
  suspend fun insert(activity: Activity): Result<Activity?> = newQuery {
    ActivityTable.insert {
      it[owner] = activity.owner
      it[type] = activity.type
      it[timestamp] = activity.timestamp
    }
      .resultedValues
      ?.firstNotNullOfOrNull { it.toActivity() }
  }

  /** Helper method to convert a retrieved [ResultRow] from the database to an [Activity] */
  private fun ResultRow.toActivity(): Activity =
    Activity(
      id = this[ActivityTable.id],
      owner = this[ActivityTable.owner],
      type = this[ActivityTable.type],
      timestamp = this[ActivityTable.timestamp]
    )
}
