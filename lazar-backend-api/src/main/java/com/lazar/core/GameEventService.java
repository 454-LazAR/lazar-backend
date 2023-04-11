package com.lazar.core;

import com.lazar.model.*;
import com.lazar.persistence.GameRepository;
import com.lazar.persistence.GeoDataRepository;
import com.lazar.persistence.PlayerRepository;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static com.lazar.LazarApplication.DEBUG_MODE;

@Service
public class GameEventService {

    public static final Double HEADING_THRESHOLD = 10.0;
    public static final Long PING_INTERVAL = 2000L; // ms
    public static final Integer DAMAGE_PER_HIT = 20;
    public static final Long TIMEOUT = PING_INTERVAL*15; // ms

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GeoDataRepository geoDataRepository;

    // Checks if a player ID is valid; if so, returns that Player object
    private Player checkValidPlayerId(GeoData geoData) {
        Optional<Player> player = playerRepository.getPlayerById(geoData.getPlayerId());
        if (player.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player not found for ID: " + geoData.getPlayerId());
        }
        return player.get();
    }

    // Checks for valid playerId while also returning the player's most recent geoData timestamp
    private Player checkRecentValidPlayer(GeoData geoData) {
        Optional<Player> player = playerRepository.getRecentPlayerById(geoData.getPlayerId());
        if (player.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player not found for ID: " + geoData.getPlayerId());
        }
        return player.get();
    }

    private Game getGameFromPlayerId(Player player) {
        Optional<Game> currGame = gameRepository.getGame(player.getGameId());
        if (currGame.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game doesn't exist.");
        }
        return currGame.get();
    }

    public Ping lobbyPing(GeoData geoData) {
        // ensure valid player UUID
        Player player = checkValidPlayerId(geoData);

        Game currGame = getGameFromPlayerId(player);
        geoData.setGameId(currGame.getId());

        if(currGame.getGameStatus() == Game.GameStatus.ABANDONED) {
            return new Ping(Game.GameStatus.ABANDONED, null, null, null);
        } else if (!DEBUG_MODE && Duration.between(currGame.getLatestGameStatusUpdate(), Instant.now()).toMillis() >= TIMEOUT) {
            gameRepository.updateGameStatus(currGame.getId(), Game.GameStatus.ABANDONED);
            return new Ping(Game.GameStatus.ABANDONED, null, null, null);
        }

        if (player.getIsAdmin() && !gameRepository.updateLastActivity(currGame.getId(), Instant.now())) {
            String errorMessage = "Failed to update the last activity of game " + currGame.getId();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        }

        // check if we're in the lobby or if the game has started
        if (currGame.getGameStatus() == Game.GameStatus.IN_LOBBY) {
            List<String> currPlayers = playerRepository.getUsernamesByGame(geoData.getGameId());
            return new Ping(Game.GameStatus.IN_LOBBY, null, null, currPlayers);
        }
        // Game has started, return in-game ping so the user knows the game has started
        else {
            return new Ping(currGame.getGameStatus(), null, player.getHealth(), null);
        }
    }

    public Ping gamePing(GeoData geoData) {
        // ensure valid longitude, latitude, timestamp
        if (geoData.getLongitude() == null || geoData.getLatitude() == null || geoData.getTimestamp() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Must specify longitude, latitude, and timestamp.");
        }

        Player player = checkRecentValidPlayer(geoData);

        if (player.getIsInactive()) {
            return new Ping(null, true, null, null);
        }

        Game game = getGameFromPlayerId(player);
        if(game.getGameStatus() == Game.GameStatus.IN_LOBBY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game has not started.");
        }

        // DEBUG_MODE = false -> we are checking for inactivity
        // Second Clause -> Checks if the user is inactive relative to their last ping.
        // Third Clause -> Checks if the user is inactive relative to the start of the game.
        if(!DEBUG_MODE
                && (player.getLastUpdateTime() == null || Duration.between(player.getLastUpdateTime(), Instant.now()).toMillis() >= TIMEOUT)
                && Duration.between(game.getLatestGameStatusUpdate(), Instant.now()).toMillis() >= TIMEOUT) {
            playerRepository.updateInactive(player.getId());
            return new Ping(null, true, null, null);
        }

        // update database with player location and timestamp via populating and passing the geoData object
        geoData.setGameId(game.getId());
        try {
            if(!geoDataRepository.insertPing(geoData)){
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred inserting the ping into the DB.");
            }
        } catch (UnableToExecuteStatementException e) {
            // Duplicate entry for (playerId, timestamp, latitude, longitude)
            // This situation is completely impossible outside of postman testing.
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred inserting the ping into the DB.");
        }

        // get health
        Optional<Integer> health = playerRepository.getPlayerHealth(geoData.getPlayerId());
        // couldn't find health in database, something went wrong
        if (health.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Player health not found in database.");
        }

        return new Ping(game.getGameStatus(), null, health.get(), null);
    }

    public boolean checkHit(GeoData geoData) {

        Player player = checkRecentValidPlayer(geoData);
        geoData.setGameId(player.getGameId());

        if (player.getIsInactive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player with ID " + geoData.getPlayerId() + " is inactive.");
        }

        if(player.getHealth() == 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Player is already dead.");
        }

        Optional<Game> game = gameRepository.getGame(player.getGameId());
        if(game.isPresent() && game.get().getGameStatus() != Game.GameStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is still in the lobby or has finished.");
        }

        // Same check as above.
        if(!DEBUG_MODE
                && Duration.between(player.getLastUpdateTime(), Instant.now()).toMillis() >= TIMEOUT
                && Duration.between(game.get().getLatestGameStatusUpdate(), Instant.now()).toMillis() >= TIMEOUT) {
            playerRepository.updateInactive(player.getId());
            return false;
        }

        // Get a list of all players geo data
        List<GeoData> playerLocations = geoDataRepository.getGeoDataForHitCheck(geoData);
        for(GeoData playerLocation : playerLocations) {
            // Calculate relative heading from the shooter.
            double diff = Math.abs(geoData.getHeading() - geoData.bearingTo(playerLocation));
            if (diff > 180) {
                diff = 360 - diff;
            }
            playerLocation.setHeading(diff);
        }
        playerLocations.sort(Comparator.comparing(GeoData::getHeading));

        if(playerLocations.isEmpty()) {
            gameRepository.updateGameStatus(geoData.getGameId(), Game.GameStatus.FINISHED);
            return false;
        }

        if (playerLocations.get(0).getHeading() > HEADING_THRESHOLD) {
            return false;
        }

        int decrementBy = DAMAGE_PER_HIT;
        if(!playerRepository.updateHealth(playerLocations.get(0).getPlayerId(), decrementBy)){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating player in database.");
        }

        if(playerLocations.size() == 1) {
            checkGameOver(playerLocations.get(0).getPlayerId(), geoData.getGameId());
        }

        return true;
    }

    private void checkGameOver(UUID playerId, String gameId) {

        Optional<Integer> health = playerRepository.getPlayerHealth(playerId);
        if(health.isEmpty() || health.get() != 0) {
            return;
        }

        gameRepository.updateGameStatus(gameId, Game.GameStatus.FINISHED);

    }

}
