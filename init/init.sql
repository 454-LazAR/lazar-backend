
CREATE DATABASE lazardb;

GRANT ALL PRIVILEGES ON lazardb.* TO 'lazar'@'%';

USE lazardb;

CREATE TABLE games (
	id INT PRIMARY KEY,
	gameStatus ENUM('IN_PROGRESS', 'IN_LOBBY', 'FINISHED')
);

CREATE TABLE players (
	id INT PRIMARY KEY,
	gameId INT,
	username VARCHAR(30),
	health INT,
	isAdmin BOOLEAN,
	-- GPS Data
	FOREIGN KEY (gameId) REFERENCES Games(id)
);
