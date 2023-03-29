package com.lazar.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class GeoData implements Serializable {

    public final static double EARTH_RADIUS = 6371e3; // Earth's radius in meters

    public Double bearingTo(GeoData firingAt) {
        double lat1 = Math.toRadians(this.latitude);
        double lon1 = Math.toRadians(this.longitude);
        double lat2 = Math.toRadians(firingAt.latitude);
        double lon2 = Math.toRadians(firingAt.longitude);

        double y = Math.sin(lon2 - lon1) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }

    public Double distanceTo(GeoData firingAt) {
        double lat1 = Math.toRadians(this.latitude);
        double lat2 = Math.toRadians(firingAt.latitude);
        double deltaLat = Math.toRadians(firingAt.latitude - this.latitude);
        double deltaLon = Math.toRadians(firingAt.longitude - this.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS * c;

        return distance;
    }

    private UUID playerId;
    private String gameId;
    private Double longitude;
    private Double latitude;
    private Double heading;
    private Timestamp timestamp;
}