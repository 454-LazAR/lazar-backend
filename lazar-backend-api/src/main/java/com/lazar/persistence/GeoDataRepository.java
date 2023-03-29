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

}
