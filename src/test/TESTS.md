# Testing

Testing in dChat follows a very simple philosophy; don't repeat yourself. Often-times when designing tests, people end
up just rewriting the code- but in a test. This leads to tightly coupled tests (which may or may not be what you want)
and takes a lot of time to maintain. So I decided against it.

Tests in dChat exist at the business layer, leaving the rest of the infrastructure alone. But, in order for this
philosophy to be practical- it means that the system must be designed in a way that facilitates *most* logic to the
business layer. You can see this in practice with dChat, as services outside of routes live fairly idempotent. They're
modular, are very simple to follow and most importantly- they won't change often
(if at all).

## Routes vs WebSockets

Tests are split into [currently] two separate categories; Routes and WebSockets.

Routes are the life-line of the application, and are seen as the core of dChat. They encompass the most important
logical operations, and connect the Database layer with the API layer.

WebSockets are split off into their own category, even though they are technically routes, purely due to the fact that
they must be treated differently. While in design the systems are very similar, WebSocket implementation details differ
quite a bit from your standard Route. Primarily because a lot more of the lifecycle of WebSockets is managed by dChat,
where's the vast majority of the lifecycle of Routes is managed by KTOR.

## Writing tests for Routes

Route tests implement something called the `ApplicationTestManager`. This test manager class wraps around `RouteState`
objects, and facilitates interaction with the `RouteHandler`.

The existence of this class is mainly for the sake of readability and consistency. Keeping implementation details
modular, and using the same core API, facilitates more consistent tests.

We also take advantage of the `MockDatabaseService` class. As of this writing, the `MockDatabaseService`
merely lives as a toggle for Exposed mocks- but that may change in the future as implementation details grow.

A standard Route test may look a little something like this:

```Kotlin
class LoginRouteTest : FreeSpec({
    val testManager = ApplicationTestManager(loginRouteHandler)
    MockDatabaseService.enable()

    suspend fun callHandler(request: LoginRequest): LoginResponse

    "request validation" - {
        "fails on invalid username" {}
        "fails on invalid password" {}
    }

    "works as expected" {}
})

```

The test is label in a straight forward and repeatable way: `{ROUTENAME}Test`

The test inherits from the Kotest `FreeSpec`

The test saves a reference to a `ApplicationTestManager` to use throughout the test's lifecycle

The test enables the database mocking services through `MockDatabaseService.enable()`

And the tests themselves have been seperated as two straight forward categories; **request validation**
and **works as expected**.

This is a common theme you'll see through-out all dChat tests, and is something you should strive to align with as
closely as possible. The more consistent tests are, the easier they are to write and maintain.

## Writing tests for WebSockets

WebSocket tests are very similar to **Route** tests, except that they have their own unique test
manager- `WebSocketTestManager`.

The test manager will create its own `WebSocketSession` and `WebSocketConnection`, so writing tests are actually a bit
simpler (in comparison to Routes).

A standard WebSocket test may look a little something like this:

```Kotlin
class SendMessageRouteTest : FreeSpec({
    val testManager = WebSocketTestManager()
    MockDatabaseService.enable()

    suspend fun callHandler(request: SendMessageRequest): SendMessageResponse

    "request validation" - {
        "fails on user not apart of chat" {}
        "fails on blank message" {}
    }

    "works as expected" {}
})

```

Which is almost identical to how **Route** tests look, with the minor difference that you don't have to pass the method
we're testing into the test manager.*

*The reason we can do this is because we facilitate most of the lifecycle of WebSockets, and can instead just mock the
method that handles the lifecycle with the `WebSocketRequest`. Where's with
**Routes**, we don't have enough control over the lifecycle to do that.

## Key Terms

`RouteHandler` - Typedef for core route logic, which should be tested upon.

`RouteState` - A state object that exists for all routes, and provides an easy-to-mock dependency injection for tests.

`ApplicationTestManager` - Route lifecycle manager, which facilitates interaction with the target method.

`MockDatabaseService` - Exposed mocking service, which allows us to mock database tables.

`WebSocketTestManager` - WebSocket lifecycle manager, similar to *ApplicationTestManager*, which facilitates interaction
with the target method.
