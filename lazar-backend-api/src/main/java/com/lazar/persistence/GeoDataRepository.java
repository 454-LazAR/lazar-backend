package com.lazar.persistence;

import com.lazar.model.GeoData;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository
public class GeoDataRepository {

    @Autowired
    private Jdbi jdbi;

    @Autowired
    private Properties queries;

    public List<GeoData> getGeoDataForHitCheck(GeoData shooterData) {
        // queries.getProperty("geoData.get.in.range");
        return null;
    }

    // TODO Drew, please check this query out and make sure it looks right.
    //  I'm assuming we want to just enter a new row for each ping instead of updating one row for each player?
    public boolean insertPing(GeoData data) {
        Integer status = jdbi.withHandle(h -> h.createUpdate(queries.getProperty("geoData.add.ping"))
            .bind("playerId", data.getPlayerId())
            .bind("gameId", data.getGameId())
            .bind("longitude", data.getLongitude())
            .bind("latitude", data.getLatitude())
            .bind("timeReceived", data.getTimestamp())
            .execute());
        return status == 1;
    }

}
