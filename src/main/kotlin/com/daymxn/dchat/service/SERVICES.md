# Services

The **Service Layer** sits between the **Business Layer** (or routes) and the **Database Layer**.

The main job of a Service is to expose short and simple type friendly methods for interacting with any given
Database `table` class defined in the `datamodel` directory.

## Designing a service

A Service is defined for each Database `table` that needs to be interacted with. All service interaction will be through
the `DatabaseManager` class, and as such; should not represent business logic. A Service function should be simple,
short, and non-specific. There *are* exceptions to that last rule though- and that would be when a more specific query
would improve processing times by a significant amount.

A standard Service may look a little something like this:

```Kotlin
object UserService {

    suspend fun getAll(): List<User> = newSuspendedTransaction {
        UserTable.selectAll().mapNotNull {
            it.toUser()
        }
    }

    suspend fun getById(id: Long): Result<User?> = newQuery {
        UserTable.select {
            UserTable.id eq id
        }.firstNotNullOfOrNull {
            it.toUser()
        }
    }

    suspend fun getByUsernameLike(str: String): List<User> = newSuspendedTransaction {
        UserTable.select {
            UserTable.username like "%$str%"
        }.mapNotNull {
            it.toUser()
        }
    }

    suspend fun getByUsername(username: String): Result<User?> = newQuery {
        UserTable.select {
            UserTable.username eq username
        }.firstNotNullOfOrNull {
            it.toUser()
        }
    }

    suspend fun update(user: User): Boolean = newSuspendedTransaction {
        UserTable.update({ UserTable.id eq user.id }) {
            it[username] = user.username
            it[password] = user.password
        }
    } == 1

    suspend fun insert(user: User): Result<User?> = newQuery {
        UserTable.insert {
            it[username] = user.username
            it[password] = user.password
        }.resultedValues?.firstNotNullOfOrNull {
            it.toUser()
        }
    }

    private fun ResultRow.toUser(): User = User(id = this[UserTable.id],
            username = this[UserTable.username],
            password = this[UserTable.password],
            isAdmin = this[UserTable.isAdmin])
}
```

A couple key things to note;

- When a response is a single object, we wrap it in a nullable result and use `newQuery` instead
  of `newSuspendedTransaction`
- Since Database results come in a `ResultRow`, we create a private extension method to convert the results into a
  Datamodel object (in this case, `ResultRow.toUser()`)
- Database queries are made through the `table` object from the `datamodel` subdirectory. We utilize exposed as our ORM
  framework, so you can read their documentation to get a better idea of how queries are structured
