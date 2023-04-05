package com.lazar.core;

import com.lazar.model.*;
import com.lazar.persistence.GameRepository;
import com.lazar.persistence.GeoDataRepository;
import com.lazar.persistence.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static com.lazar.LazarApplication.DEBUG_MODE;

@Service
public class GameEventService {

    public static final Double HEADING_THRESHOLD = 10.0;
    public static final Long PING_INTERVAL = 1000L; // ms
    public static final Integer DAMAGE_PER_HIT = 20;
    public static final Long TIME_THRESHOLD = PING_INTERVAL*3; // ms
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid player ID.");
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

        // check if we're in the lobby or if the game has started
        if (currGame.getGameStatus() == Game.GameStatus.IN_LOBBY) {
            List<String> currPlayers = playerRepository.getUsernamesByGame(geoData.getGameId());
            return new Ping(Game.GameStatus.IN_LOBBY, null, currPlayers);
        }
        // Game has started, return in-game ping so the user knows the game has started
        else {
            return new Ping(currGame.getGameStatus(), player.getHealth(), null);
        }
    }

    public Ping gamePing(GeoData geoData) {
        Player player = checkValidPlayerId(geoData);
        Game game = getGameFromPlayerId(player);

        if(game.getGameStatus() == Game.GameStatus.IN_LOBBY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game has not started.");
        }

        // get health
        Optional<Integer> health = playerRepository.getPlayerHealth(geoData.getPlayerId());
        // couldn't find health in database, something went wrong
        if (health.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Player health not found in database.");
        }

        // ensure valid longitude, latitude, timestamp
        if (geoData.getLongitude() == null || geoData.getLatitude() == null || geoData.getTimestamp() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Must specify longitude, latitude, and timestamp.");
        }
        // update database with player location and timestamp via populating and passing the geoData object
        geoData.setGameId(game.getId());
        if (!geoDataRepository.insertPing(geoData)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred inserting the ping into the DB.");
        }

        return new Ping(game.getGameStatus(), health.get(), null);
    }

    public boolean checkHit(GeoData geoData) {
        // Find game id, update geoData object
        Player player = checkValidPlayerId(geoData);
        geoData.setGameId(player.getGameId());

        if(player.getHealth() == 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Player is already dead.");
        }

        Optional<Game> game = gameRepository.getGame(player.getGameId());
        if(game.isPresent() && game.get().getGameStatus() != Game.GameStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is still in the lobby or has finished.");
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

        GeoData hitPlayer = playerLocations.isEmpty() ? null : playerLocations.get(0);
        GameInfo gameInfo = getGameInfo(game.get(), hitPlayer == null ? null : hitPlayer.getPlayerId());
        if (gameInfo.getStatus() == Game.GameStatus.FINISHED || hitPlayer.getHeading() > HEADING_THRESHOLD) {
            return false;
        }

        int decrementBy = DAMAGE_PER_HIT;
        if(!playerRepository.updateHealth(hitPlayer.getPlayerId(), decrementBy)){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating player in database.");
        }

        if(gameInfo.getNumAlivePlayers() == 2 && gameInfo.getFocusPlayerHealth() - decrementBy <= 0) {
            gameRepository.updateGameStatus(game.get().getId(), Game.GameStatus.FINISHED);
        }

        return true;
    }

    // This would be much simpler with websockets I think
    // Fetches all players from a game, returns the health of the focus player (player who's health is about to be changed)
    // Detects inactive players, sets their health to 0
    // Debug mode = true -> does not check for inactive users
    private GameInfo getGameInfo(Game game, UUID focusPlayerId) {
        List<Player> players = playerRepository.getPlayerLatestData(game.getId());

        List<UUID> inactivePlayers = new ArrayList<>();
        int numAlivePlayers = 0;
        GameInfo gameInfo = new GameInfo();
        gameInfo.setGame(game);

        for(Player player : players) {
            if(!DEBUG_MODE && Duration.between(player.getLastUpdateTime(), Instant.now()).toMillis() >= TIMEOUT){
                inactivePlayers.add(player.getId());
            } else {
                numAlivePlayers++;
                if(player.getId().equals(focusPlayerId)) {
                    gameInfo.setFocusPlayerHealth(player.getHealth());
                }
            }
        }
        gameInfo.setNumAlivePlayers(numAlivePlayers);

        if(!inactivePlayers.isEmpty()){
            playerRepository.killInactivePlayers(inactivePlayers);
        }

        if(numAlivePlayers <= 1) {
            gameInfo.setStatus(Game.GameStatus.FINISHED);
            gameRepository.updateGameStatus(game.getId(), Game.GameStatus.FINISHED);
        }

        return gameInfo;
    }

}
