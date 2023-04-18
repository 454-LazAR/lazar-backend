package com.lazar.persistence;

import com.lazar.core.GameEventService;
import com.lazar.model.GeoData;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Time;
import java.sql.Timestamp;
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
        return jdbi.withHandle(h -> h.createQuery(queries.getProperty("geoData.get.in.range"))
                .bind("gameId", shooterData.getGameId())
                .bind("playerId", shooterData.getPlayerId())
                .map((r,c) -> new GeoData(r.getString(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5)))
                .list());
    }

    // TODO Drew, please check this query out and make sure it looks right.
    //  I'm assuming we want to just enter a new row for each ping instead of updating one row for each player?
    public boolean insertPing(GeoData data) throws UnableToExecuteStatementException {
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
