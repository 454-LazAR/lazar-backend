package com.lazar.persistence;

import com.lazar.core.GameEventService;
import com.lazar.model.GeoData;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Properties;

@Repository
public class GeoDataRepository {

    @Autowired
    private Jdbi jdbi;

    @Autowired
    private Properties queries;

    public List<GeoData> getGeoDataForHitCheck(GeoData shooterData) {
        Instant timestamp = shooterData.getTimestamp().toInstant();
        return jdbi.withHandle(h -> h.createQuery(queries.getProperty("geoData.get.in.range"))
                .bind("gameId", shooterData.getGameId())
                .bind("playerId", shooterData.getPlayerId())
                .bind("min", timestamp.minusMillis(GameEventService.TIME_THRESHOLD))
                .bind("max", timestamp.plusMillis(GameEventService.TIME_THRESHOLD))
                .map((r,c) -> new GeoData(r.getString(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5)))
                .list());
    }

}
