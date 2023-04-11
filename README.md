# lazar-backend
Docker config files and API code for the backend server of the LazAR augmented reality laser tag app.


# API Documentation

## At a Glance

All routes are relative to `http://143.244.200.36:8080` (Prod) or `http://localhost:8080` (Dev)

All game-functionality requests must have a valid player UUID.

| Method | URL     | Purpose                                    | Return Codes            |
|--------|---------|--------------------------------------------|-------------------------|
| `GET`  | `/hello-world` | Test your connection to the API    | 200 |
| `POST` | `/create`| Create a new game | 200, 400, 500 |
| `POST` | `/join` | Join a game with a game id and a username | 200, 400, 404, 409, 500 |
| `POST` | `/lobby-ping` | Get a list of players and see if the game has started | 200, 400, 404 |
| `POST` | `/game-ping` | Update the server with a player's location and receive game status | 200, 400, 404, 500 |
| `POST` | `/start` | Start the game | 200, 400, 401, 403, 404, 409, 500 |
| `POST` | `/check-hit` | Shoot another player | 200, 400, 404, 500 |

## In-Depth Explanations

### Hello world
`GET` `http://143.244.200.36:8080/hello-world`

**Example Request Body**

No request body is required.

A `200` will be sent with a friendly message from the server.
```
Hello world!
```

### Create a game
`POST` `http://143.244.200.36:8080/create`

**Example Request Body**

```json
{
    "username": "drew"
}
```

A `200` will be sent with the player's UUID and gameId.
```json
{
  "id": "64a4747a-5e64-4959-9918-a41fce099320",
  "gameId": "lud2d6"
}
```

A `400` will be sent if the username is not specified.
```json
{
  "timestamp": "2023-03-28T06:08:56.777+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Must specify username.",
  "path": "/create"
}
```

A `500` will be sent if the server cannot add the user to the database.
```json
{
  "timestamp": "2023-03-28T06:08:56.777+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Error adding player to database.",
  "path": "/create"
}
```

### Join a game
`POST` `http://143.244.200.36:8080/join`

**Example Request Body**

```json
{
  "username": "drew",
  "gameId": "lud2d6"
}
```

A `200` will be sent with the player's UUID and gameId.
```json
{
  "id": "64a4747a-5e64-4959-9918-a41fce099320",
  "gameId": "lud2d6"
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
`POST` `http://143.244.200.36:8080/lobby-ping`


**Example Request Body**

```json
{
    "playerId": "519acf16-80e4-409a-b053-224a562c237d"
}
```

A `200` will be sent with the game's status and a list of players in the lobby. If the game has started, a `game-ping` will be returned instead. If the API has not received a ping from the admin in at least 30 seconds (15*ping_interval), the gameStatus will be marked as `ABANDONED`, and users still connected to the lobby should disconnect.
```json
{
    "gameStatus": "IN_LOBBY",
    "usernames": [
        "drew",
        "harrison"
    ]
}
```

```json
{
    "gameStatus": "ABANDONED"
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

A `200` will be sent with the game's status and the player's health. If a player has not pinged the server in 30 seconds (15*ping_interval), they will be marked as inactive. If a user is marked inactive, they are essentially dead. The front end should indicate to the user that they were kicked for inactivity and should return to the main menu.

```json
{
    "gameStatus": "IN_PROGRESS",
    "health": 100
}
```

```json
{
    "isInactive": true
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
  "message": "Game has already started or has been abandoned.",
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

### Check hit
`POST` `http://143.244.200.36:8080/check-hit`

**Example Request Body**

```json
{
    "playerId": "17c4d764-8733-44eb-a990-088a6a8d5c6b",
    "timestamp": "2023-04-04T22:30:30.532Z",
    "latitude": "43.102416",
    "longitude": "-89.427879",
    "heading": "180"
}
```

A `200` will be sent with a boolean indicating whether or not the hit was successful (based on the player's heading and position relative to other players).
```
true
```

A `400` will be sent if `playerId` is invalid/doesn't exist.
```json
{
  "timestamp": "2023-03-28T06:08:56.777+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Game has not started.",
  "path": "/check-hit"
}
```

A `404` will be sent if the Game object associated with the `playerId` can't be found.
```json
{
  "timestamp": "2023-03-28T06:10:25.918+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Game doesn't exist.",
  "path": "/check-hit"
}
```

A `500` will be sent if the server encounters an error adding the hit to the database.
```json
{
  "timestamp": "2023-03-28T06:10:25.918+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Error updating player in database.",
  "path": "/check-hit"
}
```
