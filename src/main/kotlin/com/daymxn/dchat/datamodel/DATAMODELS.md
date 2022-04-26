# Datamodels

Datamodels serve as the connection between the **Service Layer** and the **Database Layer**.

A Datamodel file will have at least *two* class definitions: the `Table` class and the `Datamodel`
class. These two classes work in tangent with one another. The `Table` class serves as A schema for Exposed to apply to
the Database. The `Datamodel` class serves as a representation of what the same Database table may look like to the rest
of the code.

For example, say we have the Database table below:

```Kotlin
======
User
======
--> LONG AUTO PK [id]
--> VARCHAR UNIQUE[username]
--> VARCHAR [password]
--> BOOL [is_admin] 
```

The way we might represent this as a schema is like so:

```Kotlin
object UserTable : Table() {
    val id = long("id").autoIncrement()
    val username = varchar("username", length = 50).uniqueIndex()
    val password = varchar("password", length = 50)
    val isAdmin = bool("is_admin").default(false)

    override val primaryKey = PrimaryKey(id, name = "pk_user_id")
}
```

In turn, the way we might represent this same *database schema* to the codebase is like so:

```Kotlin
@Serializable
data class User(
    val id: Long,
    val username: String,
    val password: String,
    val isAdmin: Boolean = false,
) : Datamodel
```

So in conclusion;

`Table` - Schema (or an outline) of how the table looks in the Database. Also directly utilized at the **Service Layer**
to interact with the Database.

`Datamodel` - Data class that defines the primary structure of the model, and how it will be represented in the rest of
the code. Used as actual instances of Database entries.

## Migration support

Exposed does not actually (at least at the time of this writing) have native support for migrations. This may be a
deal-breaker for some, and is something that should be explored in the future.
