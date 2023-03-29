
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
	-- GPS Data
	FOREIGN KEY (gameId) REFERENCES games(id)
);

CREATE TABLE geoData (
	id VARCHAR(36) FOREIGN KEY REFERENCES players(id),
	gameId VARCHAR(6) FOREIGN KEY REFERENCES games(id),
	longitude DOUBLE,
	latitude DOUBLE,
	timeReceived TIMESTAMP
);
