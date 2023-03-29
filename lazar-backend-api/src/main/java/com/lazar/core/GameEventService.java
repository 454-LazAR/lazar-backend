package com.lazar.core;

import com.lazar.model.GeoData;
import com.lazar.model.Ping;
import com.lazar.persistence.GameRepository;
import com.lazar.persistence.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameEventService {

    private static final Double HEADING_THRESHOLD = 0.0;
    private static final Double PING_INTERVAL = 0.0;
    private static final Double TIME_THRESHOLD = PING_INTERVAL*3;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    public Ping ping(GeoData geoData) {
        return null;
    }

    public void checkHit(GeoData geoData) {
        // Find game id

        // query geoData table for all timestamps from all UUID's except this UUID
        // filter to only include timestamps within x seconds of shooter's timestamp

    }

    private void checkGameOver() {

    }

}
