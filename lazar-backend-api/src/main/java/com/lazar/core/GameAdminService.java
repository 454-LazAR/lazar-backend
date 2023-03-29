package com.lazar.core;

import com.lazar.model.Game;
import com.lazar.model.Player;
import com.lazar.persistence.GameRepository;
import com.lazar.persistence.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class GameAdminService {

    public static final int MAX_HEALTH = 100;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

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

    public Player start() {
        return null;
    }
}
