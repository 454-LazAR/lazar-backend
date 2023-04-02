package com.lazar.core;

import com.lazar.model.Game;
import com.lazar.model.GeoData;
import com.lazar.model.Ping;
import com.lazar.model.Player;
import com.lazar.persistence.GameRepository;
import com.lazar.persistence.GeoDataRepository;
import com.lazar.persistence.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class GameEventService {

    private static final Double HEADING_THRESHOLD = 0.0;
    private static final Double PING_INTERVAL = 0.0;
    private static final Double TIME_THRESHOLD = PING_INTERVAL*3;
    private static final Integer DAMAGE_PER_HIT = 20;

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
            return gamePing(geoData);
        }
    }

    public Ping gamePing(GeoData geoData) {
        Player player = checkValidPlayerId(geoData);
        Game game = getGameFromPlayerId(player);

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
        Optional<Player> player = playerRepository.getPlayerById(geoData.getPlayerId());
        if(player.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Must specify valid player UUID.");
        }
        geoData.setGameId(player.get().getGameId());

        // Get a list of all players geo data
        List<GeoData> playerLocations = geoDataRepository.getGeoDataForHitCheck(geoData);
        for(GeoData playerLocation : playerLocations) {
            // Calculate relative heading from the shooter.
            // Store as the absolute value of the difference between shooter heading and shootee
            playerLocation.setHeading(Math.abs(geoData.getHeading() - geoData.bearingTo(playerLocation)));
        }
        playerLocations.sort(Comparator.comparing(GeoData::getHeading));

        if(playerLocations.get(0).getHeading() > HEADING_THRESHOLD) {
            return false;
        }

        int decrementBy = DAMAGE_PER_HIT;
        if(!playerRepository.updateHealth(geoData.getPlayerId(), decrementBy)){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating player in database.");
        }

        return true;
    }

    private void checkGameOver() {

    }

}
