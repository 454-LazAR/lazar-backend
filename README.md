# lazar-backend
The backend server's configuration files and API code


# API Documentation

## At a Glance

All routes are relative to `http://143.244.200.36:8080` (Prod) or `http://localhost:8080` (Dev)

All game-functionality requests must have a valid player UUID.

| Method | URL     | Purpose                                    | Return Codes            |
|--------|---------|--------------------------------------------|-------------------------|
| `GET`  | `/hello-world` | Test your connection to the API.    | 200 |
| `POST` | `/join` | Join a game with a game id and a username. | 200, 400, 404, 409, 500 |
| `GET` | `/lobby-ping` | Get a list of players and see if the game has started | 200, 400, 404 |
| `POST` | `/game-ping` | Update the server with a user's location and receive game status | 200, 400, 404, 500 |
| `POST` | `/start` | Start the game | 200, 400, 401, 403, 404, 409, 500 |

## In-Depth Explanations

### Hello world
`GET` `http://143.244.200.36:8080/hello-world`

**Example Request Body**

No request body is required.

A `200` will be sent with a friendly message from the server.
```
Hello world!
```

### Join a game
`GET` `http://143.244.200.36:8080/join`

**Example Request Body**

```json
{
  "username": "drew",
  "gameId": "100"
}
```

A `200` will be sent with the player's UUID and gameId.
```json
{
  "id": "64a4747a-5e64-4959-9918-a41fce099320",
  "gameId": 100
}
```

A `400` will be sent if username or gameid are not specified.
```json
{
  "timestamp": "2023-03-28T06:08:56.777+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Must specify gameId and username.",
  "path": "/join"
}
```

A `404` will be sent if the specified gameId is not valid (there is no game that exists with that ID).
```json
{
  "timestamp": "2023-03-28T06:10:25.918+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Game does not exist.",
  "path": "/join"
}
```

A `409` will be sent if the specified gameId is already in progress or has concluded.
```json
{
  "timestamp": "2023-03-28T06:11:46.455+00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Game is already in progress or has completed.",
  "path": "/join"
}
```

### Ping the lobby
`GET` `http://143.244.200.36:8080/lobby-ping`


**Example Request Body**

```json
{
    "playerId": "519acf16-80e4-409a-b053-224a562c237d"
}
```

A `200` will be sent with the game's status and a list of players in the lobby. If the game has started, a `game-ping` will be returned instead.
```json
{
    "gameStatus": "IN_LOBBY",
    "usernames": [
        "drew",
        "harrison"
    ]
}
```

A `400` will be sent if `playerId` is invalid/doesn't exist.
```json
{
  "timestamp": "2023-03-28T06:08:56.777+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid player ID.",
  "path": "/lobby-ping"
}
```

A `404` will be sent if the Game object associated with the `playerId` can't be found.
```json
{
  "timestamp": "2023-03-28T06:10:25.918+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Game doesn't exist.",
  "path": "/lobby-ping"
}
```

### Ping in-game
`POST` `http://143.244.200.36:8080/game-ping`

**Example Request Body**

```json
{
    "playerId": "17c4d764-8733-44eb-a990-088a6a8d5c6b",
    "latitude": "43.095414",
    "longitude": "-89.428867",
    "timestamp": "2023-04-04T22:30:30.532Z"
}
```

A `200` will be sent with the game's status and the player's health.
```json
{
    "gameStatus": "IN_PROGRESS",
    "health": 100
}
```

A `400` will be sent if `playerId` is invalid/doesn't exist, if the game hasn't started yet, or if `longitude`, `latitude`, or `timestamp` fields are missing.
```json
{
  "timestamp": "2023-03-28T06:08:56.777+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Game has not started.",
  "path": "/game-ping"
}
```

A `404` will be sent if the Game object associated with the `playerId` can't be found.
```json
{
  "timestamp": "2023-03-28T06:10:25.918+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Game doesn't exist.",
  "path": "/game-ping"
}
```

A `500` will be sent if the server can't find a player's health, or if the server encounters an error adding the ping to the database.
```json
{
  "timestamp": "2023-03-28T06:10:25.918+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An error occurred inserting the ping into the DB.",
  "path": "/game-ping"
}
```


### Start a game
`POST` `http://143.244.200.36:8080/start`


**Example Request Body**

```json
{
  "playerId": "64a4747a-5e64-4959-9918-a41fce099320",
}
```

A `200` will be sent with a boolean indicating that the game was started successfully.
```json
true
```

A `400` will be sent if `playerId` is invalid/doesn't exist.
```json
{
  "timestamp": "2023-03-28T06:08:56.777+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid player ID.",
  "path": "/start"
}
```

A `401` will be sent if `playerId` isn't the game admin.
```json
{
  "timestamp": "2023-03-28T06:08:56.777+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Only the game admin can start the game.",
  "path": "/start"
}
```

A `403` will be sent if there aren't at least 2 players in the game.
```json
{
  "timestamp": "2023-03-28T06:08:56.777+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Cannot start a game with less than 2 users.",
  "path": "/start"
}
```

A `404` will be sent if the Game object associated with the `playerId` can't be found.
```json
{
  "timestamp": "2023-03-28T06:10:25.918+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Game doesn't exist.",
  "path": "/start"
}
```

A `409` will be sent if the game associated with `playerId` does not have a status of  `IN_LOBBY`.
```json
{
  "timestamp": "2023-03-28T06:11:46.455+00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Cannot start a game if not in lobby.",
  "path": "/start"
}
```

A `500` will be sent if the server encounters any other errors, especially related to updating the game's status to `IN_PROGRESS`.
```json
{
  "timestamp": "2023-03-28T06:08:56.777+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Error starting game.",
  "path": "/start"
}
```

# Example API Documentation from CS571 

### Getting Messages for Chatroom

`GET` `https://www.cs571.org/s23/hw6/api/chatroom/:chatroomName`

There is no get all messages; you must get messages for a particular `:chatroomName`. All messages are public, you do *not* need to be logged in to access them. Only up to the latest 25 messages will be returned. A `200` (new) or `304` (cached) response will be sent with messages organized from most recent to least recent. Note that the `created` field is in Unix epoch time.

```json
{
    "msg": "Successfully got the latest messages!",
    "messages": [
        {
            "id": 2,
            "poster": "acct123",
            "title": "My Test Post",
            "content": "lorem ipsum dolor sit",
            "chatroom": "Vilas",
            "created": 1677515453383
        },
        {
            "id": 1,
            "poster": "acct123",
            "title": "My Test Post",
            "content": "lorem ipsum dolor sit",
            "chatroom": "Vilas",
            "created": 1677515193610
        }
    ]
}
```

If a chatroom is specified that does not exist, a `404` will be returned.

```json
{
    "msg": "The specified chatroom does not exist. Chatroom names are case-sensitive."
}
```

### Registering a User
`POST` `https://www.cs571.org/s23/hw6/api/register`

You must register a user with a specified `username` and `password`. 

Requests must include credentials as well as a header `Content-Type: application/json`.

**Example Request Body**

```json
{
    "username": "test12456",
    "password": "p@ssw0rd1"
}
```

If the registration is successful, the following `200` will be sent...
```json
{
    "msg": "Successfully created user!",
    "user": {
        "id": 4,
        "username": "test12456"
    }
}
```

A `Set-Cookie` response header will include your JWT in `badgerchat_auth`. This is *not* accessible by JavaScript. The provided token is an irrevocable JWT that will be valid for **1 hour**. All future requests that include credentials will send this cookie with the request.

If you forget to include a `username` or `password`, the following `400` will be sent...

```json
{
    "msg": "A request must contain a 'username' and 'password'"
}
```

If a user by the requested `username` already exists, the following `409` will be sent...

```json
{
    "msg": "The user already exists!"
}
```

If the `username` is longer than 64 characters or if the `password` is longer than 128 characters, the following `413` will be sent...

```json
{
    "msg": "'username' must be 64 characters or fewer and 'password' must be 128 characters or fewer"
}
```

### Logging in to an Account

`POST` `https://www.cs571.org/s23/hw6/api/login`

You must log a user in with their specified `username` and `password`.

Requests must include credentials as well as a header `Content-Type: application/json`.

**Example Request Body**

```json
{
    "username": "test12456",
    "password": "pass123"
}
```

If the login is successful, the following `200` will be sent...

```json
{
    "msg": "Successfully authenticated.",
    "user": {
        "id": 4,
        "username": "test12456"
    }
}
```

A `Set-Cookie` response header will include your JWT in `badgerchat_auth`. This is *not* accessible by JavaScript. The provided token is an irrevocable JWT that will be valid for **1 hour**. All future requests that include credentials will send this cookie with the request.

If you forget the `username` or `password`, the following `400` will be sent...

```json
{
    "msg": "A request must contain a 'username' and 'password'"
}
```

If the `username` exists but the `password` is incorrect, the following `401` will be sent...

```json
{
    "msg": "Incorrect password."
}
```

If the `username` does not exist, the following `404` will be sent...

```json
{
    "msg": "That user does not exist!"
}
```

### Posting a Message

`POST` `https://www.cs571.org/s23/hw6/api/chatroom/:chatroomName/messages`

Posting a message is a protected operation; you must have a valid `badgerchat_auth` session. The `:chatroomName` must be specified in the URL, and a post must also have a `title` and `content`.

Requests must include credentials as well as a header `Content-Type: application/json`.

**Example Request Body**

```json
{
    "title": "My Test Post",
    "content": "lorem ipsum dolor sit"
}
```

If the post is successful, the following `200` will be sent...

```json
{
    "msg": "Successfully posted message!"
}
```

If you forget the `title` or `content`, the following `400` will be sent...

```json
{
    "msg": "A request must contain a 'title' and 'content'"
}
```

If authentication fails (such as an expired token), the following `401` will be sent...

```json
{
    "msg": "You must be logged in to make a post!"
}
```

If a chatroom is specified that does not exist, a `404` will be returned.

```json
{
    "msg": "The specified chatroom does not exist. Chatroom names are case-sensitive."
}
```

If the `title` is longer than 128 characters or if the `content` is longer than 1024 characters, the following `413` will be sent...
```json
{
    "msg": "'title' must be 128 characters or fewer and 'content' must be 1024 characters or fewer"
}
```

### Deleting a Message
`DELETE` `https://www.cs571.org/s23/hw6/api/chatroom/:chatroomName/messages/:messageId`

Posting a message is a protected operation; you must have a valid `badgerchat_auth` session. The `:chatroomName` and `:messageId` must be specified in the URL.

Requests must include credentials. There is no request body for this request.

If the delete is successful, the following `200` will be sent...

```json
{
    "msg": "Successfully deleted message!"
}
```

If authentication fails (such as an expired token), the following `401` will be sent...

```json
{
    "msg": "You must be logged in to make a post!"
}
```

If you try to delete another user's post, the following `401` will be sent...

```json
{
    "msg": "You may not delete another user's post!"
}
```

If a chatroom is specified that does not exist, a `404` will be returned.

```json
{
    "msg": "The specified chatroom does not exist. Chatroom names are case-sensitive."
}
```

If a message is specified that does not exist, a `404` will be returned.

```json
{
    "msg": "That message does not exist!"
}
```

### Logging out
`POST` `https://cs571.org/s23/hw6/api/logout`

Logging out will cause the server to respond with a `Set-Cookie` header that will overwrite and delete the `badgerchat_auth`. 

Requests must include credentials. There is no request body for this request.

The following `200` will be sent...

```json
{
    "msg": "You have been logged out! Goodbye."
}
```

### Who Am I?
`GET` `https://cs571.org/s23/hw6/api/whoami`

This endpoint will check if a user is logged in and who they claim to be, including when their token was issued and when it will expire in Unix epoch time.

This request must include credentials. There is no request body for this request.

The following `200` will be sent...

```json
{
    "user": {
        "id": 1,
        "username": "acct123",
        "iat": 1677545250,
        "exp": 1677548850
    }
}
```

If the user is not logged in or has an invalid/expired `badgerchat_auth`, the following `401` will be sent...

```json
{
    "msg": "Missing 'badgerchat_auth' cookie. Are you logged in?"
}
```
