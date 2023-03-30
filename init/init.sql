
GRANT ALL PRIVILEGES ON lazardb.* TO 'lazar'@'%';

USE lazardb;

CREATE TABLE games (
	id VARCHAR(6) PRIMARY KEY,
	gameStatus ENUM('IN_PROGRESS', 'IN_LOBBY', 'FINISHED')
);

CREATE TABLE players (
	id VARCHAR(36) PRIMARY KEY,
	gameId VARCHAR(6),
	username VARCHAR(30),
	health INT,
	isAdmin BOOLEAN,
	FOREIGN KEY (gameId) REFERENCES games(id) ON DELETE CASCADE
);

CREATE TABLE geoData (
	playerId VARCHAR(36),
	gameId VARCHAR(6),
	longitude DOUBLE,
	latitude DOUBLE,
	timeReceived TIMESTAMP,
	FOREIGN KEY (playerId) REFERENCES players(id) ON DELETE CASCADE,
	FOREIGN KEY (gameId) REFERENCES games(id) ON DELETE CASCADE
);
