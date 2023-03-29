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

import java.util.List;
import java.util.Optional;

@Service
public class GameEventService {

    private static final Double HEADING_THRESHOLD = 0.0;
    private static final Double PING_INTERVAL = 0.0;
    private static final Double TIME_THRESHOLD = PING_INTERVAL*3;

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
        return new Ping(currGame.get().getGameStatus(), playerRepository.getPlayerHealth(geoData.getPlayerId()), null);
    }

    public void checkHit(GeoData geoData) {
        // Find game id

        // query geoData table for all timestamps from all UUID's except this UUID
        // filter to only include timestamps within x seconds of shooter's timestamp

    }

    private void checkGameOver() {

    }

}
