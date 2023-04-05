package com.lazar.core;

import com.lazar.model.Game;
import com.lazar.model.GeoData;
import com.lazar.model.Player;
import com.lazar.persistence.GameRepository;
import com.lazar.persistence.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.lazar.LazarApplication.DEBUG_MODE;

@Service
public class GameAdminService {

    public static final int MAX_HEALTH = 100;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    // These are basically the same as the methods from GameEventService.java. Feels lame to copy and paste,
    // but idk how else to reuse the code since it can't be made a static method.
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

    private static String generateUniqueGameId() {
        byte[] randomBytes = new byte[10];
        SECURE_RANDOM.nextBytes(randomBytes);
        String base64String = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String alphanumericString = base64String.replaceAll("[^a-zA-Z0-9]", "");
        return alphanumericString.substring(0, 6).toLowerCase();
    }

    public Player create(Player playerDetails) {
        if(playerDetails.getUsername() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Must specify username.");
        }
        String gameId;
        do {
            gameId = generateUniqueGameId();
        } while(!gameRepository.insertGame(gameId));
        playerDetails.setGameId(gameId);
        playerDetails.setIsAdmin(true);
        return addPlayerToGame(playerDetails);
    }

    public Player join(Player playerDetails) {
        if(playerDetails.getGameId() == null || playerDetails.getUsername() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Must specify gameId and username.");
        }
        Optional<Game> game = gameRepository.getGame(playerDetails.getGameId());
        if(game.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game does not exist.");
        } else if(game.get().getGameStatus() != Game.GameStatus.IN_LOBBY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game is already in progress or has completed.");
        }
        return addPlayerToGame(playerDetails);
    }

    private Player addPlayerToGame(Player playerDetails) {
        playerDetails.setId(UUID.randomUUID());
        playerDetails.setHealth(MAX_HEALTH);
        if(playerRepository.getUsernamesByGame(playerDetails.getGameId()).contains(playerDetails.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already used in this game.");
        }
        if(!playerRepository.insertPlayer(playerDetails)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding player to database.");
        }
        return new Player(playerDetails.getId(), playerDetails.getGameId());
    }

    public boolean start(GeoData geoData) {
        Player player = checkValidPlayerId(geoData);
        // ensure player is authorized to start the game
        if (!DEBUG_MODE && !player.getIsAdmin()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only the game admin can start the game.");
        }

        Game game = getGameFromPlayerId(player);
        // check that there are at least 2 players in the game
        List<String> players = playerRepository.getUsernamesByGame(game.getId());
        if (players.size() < 2) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot start a game with less than 2 users.");
        }
        // check that we're in lobby still
        if (game.getGameStatus() != Game.GameStatus.IN_LOBBY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot start a game if not in lobby.");
        }

        if (!gameRepository.updateGameStatus(player.getGameId(), Game.GameStatus.IN_PROGRESS)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error starting game.");
        }
        return true;
    }
}
