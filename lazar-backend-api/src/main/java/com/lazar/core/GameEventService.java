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
import java.util.UUID;

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

    public Ping ping(GeoData geoData) {
        return null;
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
