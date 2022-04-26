# Routes

Routes are the core of dChat, they provide the majority of the business logic consumed throughout the application.
Routes live slightly adjacent to the **Service Layer** (as interactions with the service layer are actually facilitated
through the `DatabaseManager`), and are directly consumed by the **API Layer**.

## Lifecycle

Routes are initially consumed via the **API Layer**, more specifically through the
`Application.configureRouting()` module. This module also handles the *initial*
authentication for protected directories. Although, this does **not** mean verification should not also be handled by
the Route. You'll see some routes also performing their own checks for user authentication. The general methodology is;
if you need access to the
`User` session object, you should verify it's existence. Especially in a kotlin world, where we want to avoid any sort
of edge-case issues with nullability.

Routes define their endpoint definitions (the URL that gets exposed via HTTP) through the
`Routes.kt` file.

Routes then define their headers in the `Headers.kt` file. A route header refers to a data object that represents how an
acceptable request should be structured, and how responses from the Route will look as well.

Finally, a file is created for the core logic of the route, such as; `RegisterRoute.kt` or
`LoginRoute.kt`

To see a more definitive example, continue reading below.

## Designing Routes

Below, I'll walk you through the general thought process you should have when designing a new, or modifying an existing
Route.

### Route endpoints

Route endpoints live under the `routes/Routes.kt` file.

Route endpoints should be labeled in **P**ascal**C**ase, regardless of the fact that they are constant values.

Route endpoints should also try to minimize non-variable string literals. For example;

Prefer this:

```Kotlin
const val HelpRoute = "/help"
const val HelpTicketRoute = "$HelpRoute/ticket"
```

Over this:

```Kotlin
const val HelpRoute = "/help"
const val HelpTicketRoute = "/help/ticket"
```

### Route headers

Route headers live under the `routes/Headers.kt` file.

Header Requests should describe, in the most strict sense, what is an acceptable request. For example, that may mean
using `ULong` in place of `Long` whenever the variable should never be less than zero.

Header Responses should describe what a Response from the server might look like. All variables must be nullable and
default to null- as this allows our error handling utility method to automagically generate responses. Furthermore,
every Response should **at-least** define an error property. It is not required that a Response have a unique return
property- but it must always have an error property.

### Route definitions

Route logic follows a two path system;

- Verify the request
- Handle the request

That usually follows the principle of implementing a "verified" version of the Request object. Those familiar with
standard functional Scala principles will feel right at home with this.

Once the request has been verified, its common to then interact with the `DatabaseManager`, and submit queries for the
data needed (or action being performed).

A standard Route may look a little something like this:

```Kotlin
fun Route.login() {
    post(LoginRoute) {
        loginRouteHandler(routeState())
    }
}

val loginRouteHandler: RouteHandler = {
    handleApplicationErrors(LoginResponse::class) {
        validateRequest(getRequest()).also {
            DatabaseManager.validateUser(it.user).apply {
                respond(
                    HttpStatusCode.Accepted,
                    LoginResponse(JWTManager.createToken(this))
                )
            }
        }
    }
}

private fun validateRequest(request: LoginRequest) =
 ValidatedLoginRequest(
    validateUsername(request.username),
    validatePassword(request.password)
 )

private fun validateUsername(username: String) =
    username.takeIf { it.isNotBlank() } ?: validationError("Username is a required field")

private fun validatePassword(password: String) =
    password.takeIf { it.isNotBlank() } ?: validationError("Password is a required field")

private data class ValidatedLoginRequest(val user: User)
```

A couple key things to note;

- We extend KTOR's `Route` class to connect the **API Layer** with our route handler
- We define how our route will be exposed, by referencing the route endpoint through `post(LoginRoute)`
- We create a `RouteHandler`, which is just a method that get called with a `RouteState` object, and handles the logic
  for the actual Route
- We wrap all of our logic in a `handleApplicationErrors` call where we specify what our response object will
  be; `LoginResponse::class`
- We validate the request, and throw an error via `validationError()` when something goes wrong
- We respond to the request with an `HttpStatusCode` and an instance of the Response object for the request, containing
  the data the user requested
