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

    public static final Double HEADING_THRESHOLD = 10.0;
    public static final Long PING_INTERVAL = 1000L; // ms
    public static final Integer DAMAGE_PER_HIT = 20;
    public static final Long TIME_THRESHOLD = PING_INTERVAL*3; // ms

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

    public Ping lobbyPing(GeoData geoData) {
        // ensure valid player UUID
        Player player = checkValidPlayerId(geoData);

        // check game status
        Optional<Game> currGame = gameRepository.getGame(player.getGameId());
        // game doesn't exist
        if (currGame.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game doesn't exist.");
        }
        geoData.setGameId(currGame.get().getId());

        if (currGame.get().getGameStatus() == Game.GameStatus.IN_LOBBY) {
            List<String> currPlayers = playerRepository.getUsernamesByGame(geoData.getGameId());
            return new Ping(Game.GameStatus.IN_LOBBY, null, currPlayers);
        }
        // Game has started, return in-game ping so the user knows the game has started
        else {
            return gamePing(geoData);
        }
    }

    public Ping gamePing(GeoData geoData) {
        // TODO
        // is there a game id
        //
        //return new Ping(currGame.get().getGameStatus(), playerRepository.getPlayerHealth(geoData.getPlayerId()), null);
        return null;
    }

    public boolean checkHit(GeoData geoData) {
        // Find game id, update geoData object
        Player player = checkValidPlayerId(geoData);
        geoData.setGameId(player.getGameId());

        // Get a list of all players geo data
        List<GeoData> playerLocations = geoDataRepository.getGeoDataForHitCheck(geoData);
        for(GeoData playerLocation : playerLocations) {
            // Calculate relative heading from the shooter.
            Double diff = Math.abs(geoData.getHeading() - geoData.bearingTo(playerLocation));
            if (diff > 180) {
                diff = 360 - diff;
            }
            playerLocation.setHeading(diff);
        }
        playerLocations.sort(Comparator.comparing(GeoData::getHeading));

        if (playerLocations.isEmpty() || playerLocations.get(0).getHeading() > HEADING_THRESHOLD) {
            return false;
        }

        int decrementBy = DAMAGE_PER_HIT;
        if(!playerRepository.updateHealth(playerLocations.get(0).getPlayerId(), decrementBy)){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating player in database.");
        }

        return true;
    }

    private void checkGameOver() {

    }

}
