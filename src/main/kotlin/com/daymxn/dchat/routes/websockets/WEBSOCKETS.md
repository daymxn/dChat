# WebSockets

WebSockets are technically a variant of Routes, and as such; are *very* similar in the way they are structured. The
biggest difference is that we at dChat actually control most of the lifecycle with WebSockets, versus with Routes where
KTOR handles the vast majority of the lifecycle. This *does* give us some freedom, but also means we have to structure
some things a bit differently.

## Lifecycle

> Note: This document assumes you have also read the `ROUTES.md` document, and will focus a bit more on the differences between WebSockets and Routes

One of the more key things to note, is that WebSockets don't technically have an endpoint definition. So you won't
actually see any endpoints in `Routes.kt` for WebSockets. That's because of the way WebSockets are consumed. The user
initially connects to the `WebSocketMain`
route, and then send *messages* through the WebSocket, versus HTTP Request to the server. So then, how do we define what
that message *is* then?

Through the `@SerialName` annotation! WebSocket headers are defined alongside Route headers in
`Headers.kt` and follow the same pattern. You'll notice though, that they have an additional annotation on them.

```Kotlin
@Serializable
data class LoginRequest(val username: String, val password: String) : Request

@Serializable
@SerialName("SendMessageRequest")
data class SendMessageRequest(val chat: ULong, val message: String) : WebSocketRequest()
```

This annotation allows us to define how the data class is translated through the WebSocket. The client can then create
an identical message (request) object to send through the WebSocket themselves. The typical pattern is to just annotate
the data class objects with the same name as their class names.

Beyond that, Routes define their core logic the same as Routes, except that the file now lives under `routes/websockets`
instead of just `routes/`. There are *some* differences as far as how handlers are defined in WebSockets, but more on
that below.

## Designing WebSockets

Below, I'll walk you through the general thought process you should have when designing a new, or modifying an existing
WebSocket.

### WebSocket endpoints

WebSocket endpoints are defined alongside their headers in `Headers.kt`. They are exposed via the
`@SerialName()` annotation, and should be labeled the same as their class name.

Endpoints should also not be nested, and so utilizing constant variables is not needed.

### WebSocket headers

WebSocket headers live under the `routes/Headers.kt` file.

Header Requests and Responses should follow the same principle of standard Routes.

### WebSocket definitions

The only difference between WebSocket definitions and Route definitions, is the absence of a named Route handler
and `RouteState`.

A standard WebSocket may look a little something like this:

```Kotlin
suspend fun WebSocketConnection.getUsersForSubstringRoute(request: GetUsersForSubstringRequest) {
    validateRequest(request).also { validatedRequest ->
        DatabaseManager.getUsersForSubstring(validatedRequest.search).also {
            session.sendMessage(GetUsersForSubstringResponse(it))
        }
    }
}

private fun validateRequest(request: GetUsersForSubstringRequest): ValidatedGetUsersForSubstringRequest =
    ValidatedGetUsersForSubstringRequest(validateSearch(request.search))

private fun validateSearch(search: String): String = search.filter { it.isLetterOrDigit() }.also {
    if (it.length < 3) validationError("Search query can not be less than 3 alphanumeric characters.")
}

private data class ValidatedGetUsersForSubstringRequest(val search: String)
```

A couple key things to note;

- WebSockets are extended through `WebSocketConnection`, and are natively wrapped in an error handler
- Inplace of a `RouteState`, WebSockets have a `WebSocketSession` object that facilitates various serializations
- WebSockets live directly under the extension, and are not split off into their own "handler" variable (like your
  standard Route and `RouteHandler`)
- WebSocket responses do **not** have a status code
